/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.util.*;
import java.util.stream.Collectors;
import net.hasor.cobble.loader.CobbleClassScanner;
import net.hasor.core.ApiBinder;

/** 一次遍历类路径，再按处理器顺序分发匹配的类。 */
public final class Scanner {
    private final List<AnnotationProcessor<Class<?>>> processors;

    @SafeVarargs
    public Scanner(AnnotationProcessor<Class<?>>... processors) {
        this.processors = List.copyOf(Arrays.asList(processors));
    }

    public void scan(ApiBinder binder, String[] packages) throws Throwable {
        if (packages.length == 0 || processors.isEmpty()) {
            return;
        }

        Set<String> annotations = processors.stream().flatMap(p -> {
            return p.annotationTypes().stream().map(Class::getName);
        }).collect(Collectors.toSet());

        Map<String, Set<String>> discovered = new HashMap<>();
        CobbleClassScanner scanner = new CobbleClassScanner(binder.getClassLoader(), binder.getResourceLoader());
        List<Class<?>> types = scanner.getClassSet(packages, context -> {
            Set<String> matched = Arrays.stream(context.getClassInfo().annos).filter(annotations::contains).collect(Collectors.toSet());
            if (matched.isEmpty()) {
                return false;
            }
            discovered.put(context.getClassInfo().className, matched);
            return true;
        }).stream().sorted(Comparator.comparing(Class::getName)).toList();

        for (AnnotationProcessor<Class<?>> processor : processors) {
            Set<String> supported = processor.annotationTypes().stream().map(Class::getName).collect(Collectors.toSet());
            List<Class<?>> matches = types.stream().filter(type -> {
                return !Collections.disjoint(discovered.get(type.getName()), supported);
            }).toList();

            processor.process(binder, matches);
        }
    }
}
