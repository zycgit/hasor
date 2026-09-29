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
import net.hasor.web.annotation.HttpMethod;
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
        Map<String, List<Mapping>> routes = new HashMap<>();
        for (Mapping mapping : binder.getMappings()) {
            registered.add(mapping.getTargetType().getBindType());
            String path = mapping.getMappingTo().replaceAll("\\{\\w+\\}", "{}");
            routes.computeIfAbsent(path, key -> new ArrayList<>()).add(mapping);
        }

        types.stream().filter(type -> {
            return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
        }).filter(type -> {
            return !registered.contains(type) && !options.getScanExcludes().contains(type.getName());
        }).sorted(Comparator.comparing(Class::getName)).forEach(type -> {
            TypeSupplier managed = new TypeSupplier() {
                private final Supplier<?> provider = binder.getProvider(type);

                @Override
                @SuppressWarnings("unchecked")
                public <T> T get(Class<? extends T> target) {
                    return (T) this.provider.get();
                }
            };

            int previousSize = binder.getMappings().size();
            binder.loadMappingTo(type, managed);
            List<Mapping> mappings = binder.getMappings();
            for (Mapping mapping : mappings.subList(previousSize, mappings.size())) {
                // Parameter names do not make two otherwise identical routes distinct.
                String path = mapping.getMappingTo().replaceAll("\\{\\w+\\}", "{}");
                List<Mapping> previous = routes.computeIfAbsent(path, key -> new ArrayList<>());
                Set<String> methods = new HashSet<>(Arrays.asList(mapping.getHttpMethodSet()));
                for (Mapping other : previous) {
                    Set<String> otherMethods = new HashSet<>(Arrays.asList(other.getHttpMethodSet()));
                    if (methods.isEmpty() || otherMethods.isEmpty() || methods.contains(HttpMethod.ANY) || otherMethods.contains(HttpMethod.ANY) || !Collections.disjoint(methods, otherMethods)) {
                        throw new IllegalStateException("Conflicting auto-scanned route " + mapping.getMappingTo() + ": " + other.getTargetType().getBindType().getName() + " and " + type.getName());
                    }
                }
                previous.add(mapping);
            }
        });
    }
}
