/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import net.hasor.cobble.asm.ClassReader;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ResourceLoader;
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
        ResourceLoader resources = binder.getResourceLoader();
        ClassLoader loader = resources.toClassLoader(binder.getClassLoader());
        String[] paths = Arrays.stream(packages).map(n -> n.replace('.', '/')).toArray(String[]::new);
        List<Class<?>> types = resources.<Class<?>>scanResources(MatchType.Prefix, event -> {
            if (!event.getName().endsWith(".class")) {
                return null;
            }

            try (InputStream input = event.getStream()) {
                ClassReader reader = new ClassReader(input);
                Set<String> matched = new HashSet<>();
                reader.accept(new ClassAnnotationCollector(annotations, matched), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                if (matched.isEmpty()) {
                    return null;
                }

                String name = reader.getClassName().replace('/', '.');
                discovered.put(name, matched);
                return loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load scanned class: " + event.getName(), e);
            }
        }, paths).stream().distinct().sorted(Comparator.comparing(Class::getName)).toList();

        for (AnnotationProcessor<Class<?>> processor : processors) {
            Set<String> supported = processor.annotationTypes().stream().map(Class::getName).collect(Collectors.toSet());
            List<Class<?>> matches = types.stream().filter(type -> {
                return !Collections.disjoint(discovered.get(type.getName()), supported);
            }).toList();

            processor.process(binder, matches);
        }
    }
}
