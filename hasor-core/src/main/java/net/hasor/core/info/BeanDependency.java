/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.util.*;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.DependsOn;

/** Internal representation of a deferred binding dependency. */
public record BeanDependency(String id, String name, Class<?> type) {
    public static final String METADATA_KEY = "hasor.dependencies";

    public BeanDependency {
        if (id != null && id.isBlank()) {
            throw new IllegalArgumentException("Dependency binding ID must not be blank");
        }

        if (id == null) {
            Objects.requireNonNull(type, "Dependency type");
        }
    }

    public static void add(BindInfo<?> binding, BeanDependency dependency) {
        List<BeanDependency> dependencies = new ArrayList<>(declared(binding));
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
        binding.setMetaData(METADATA_KEY, List.copyOf(dependencies));
    }

    @SuppressWarnings("unchecked")
    private static List<BeanDependency> declared(BindInfo<?> binding) {
        Object value = binding == null ? null : binding.getMetaData(METADATA_KEY);
        return value == null ? List.of() : (List<BeanDependency>) value;
    }

    public static List<BeanDependency> collect(BindInfo<?> binding, Class<?> type) {
        Set<BeanDependency> dependencies = new LinkedHashSet<>(declared(binding));
        DependsOn annotation = type.getAnnotation(DependsOn.class);
        if (annotation != null) {
            for (String id : annotation.value()) {
                dependencies.add(new BeanDependency(id, null, null));
            }
            for (Class<?> dependency : annotation.types()) {
                dependencies.add(new BeanDependency(null, null, dependency));
            }
        }
        return List.copyOf(dependencies);
    }

    public BindInfo<?> resolve(AppContext context) {
        if (this.id != null) {
            BindInfo<?> binding = context.getBindInfo(this.id);
            if (binding == null) {
                throw new IllegalStateException("Missing dependency binding ID: " + this.id);
            }
            return binding;
        }

        List<? extends BindInfo<?>> bindings = context.findBindingRegister(this.type).stream().filter(binding -> {
            return this.name == null || Objects.equals(this.name, binding.getBindName());
        }).toList();

        if (bindings.size() != 1) {
            throw new IllegalStateException("Expected one dependency binding for " + this.type.getName() + (this.name == null ? "" : " named '" + this.name + "'") + "; found " + bindings.size());
        }
        return bindings.get(0);
    }
}
