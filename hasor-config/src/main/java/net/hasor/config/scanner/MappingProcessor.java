/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

import javax.servlet.ServletContext;

import net.hasor.config.web.WebDefaultsModule;
import net.hasor.config.web.WebOptions;
import net.hasor.core.ApiBinder;
import net.hasor.core.TypeSupplier;
import net.hasor.web.Mapping;
import net.hasor.web.WebApiBinder;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.MappingToGroup;

/** 处理已发现的 Web 路由，由主模块在 Web 依赖可用时装配。 */
public final class MappingProcessor implements AnnotationProcessor<Class<?>> {
    @Override
    public List<Class<? extends Annotation>> annotationTypes() {
        return List.of(MappingTo.class, MappingToGroup.class);
    }

    @Override
    public void process(ApiBinder binder, List<Class<?>> types) {
        WebApiBinder webBinder = binder.tryCast(WebApiBinder.class);
        if (webBinder == null) {
            return;
        }

        ServletContext context = webBinder.getServletContext();
        Object supplied = context == null ? null : context.getAttribute(WebOptions.class.getName());
        WebOptions options = supplied instanceof WebOptions ? ((WebOptions) supplied).copy() : new WebOptions();
        register(webBinder, WebDefaultsModule.loadOptions(options, binder.getSettings()), types);
    }

    private static void register(WebApiBinder binder, WebOptions options, List<Class<?>> types) {
        Set<Class<?>> registered = new HashSet<>();
        Map<String, Class<?>> routes = new HashMap<>();
        for (Mapping mapping : binder.getMappings()) {
            registered.add(mapping.getTargetType().getBindType());
            routes.put(mapping.getMappingTo().replaceAll("\\{\\w+\\}", "{}"), mapping.getTargetType().getBindType());
        }

        types.stream().filter(type -> {
            return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
        }).filter(type -> {
            return !registered.contains(type) && !options.getScanExcludes().contains(type.getName());
        }).sorted(Comparator.comparing(Class::getName)).forEach(type -> {
            // Parameter names do not make two otherwise identical routes distinct.
            for (MappingTo mapping : type.getAnnotationsByType(MappingTo.class)) {
                for (String value : mapping.value()) {
                    String path = value.replaceAll("\\{\\w+\\}", "{}");
                    Class<?> previous = routes.putIfAbsent(path, type);
                    if (previous != null && previous != type) {
                        throw new IllegalStateException("Conflicting auto-scanned route " + value + ": " + previous.getName() + " and " + type.getName());
                    }
                }
            }

            TypeSupplier managed = new TypeSupplier() {
                private final Supplier<?> provider = binder.getProvider(type);

                @Override
                @SuppressWarnings("unchecked")
                public <T> T get(Class<? extends T> target) {
                    return (T) this.provider.get();
                }
            };
            binder.loadMappingTo(type, managed);
        });
    }
}
