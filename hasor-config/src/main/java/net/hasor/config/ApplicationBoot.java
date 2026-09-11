/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.Objects;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;

/** Starts Hasor with a primary {@link Configuration} class and the configured Core scan scope. */
public final class ApplicationBoot {
    private ApplicationBoot() {
    }

    public static Hasor create(Class<?> primarySource) {
        return configure(Hasor.create(), primarySource);
    }

    public static Hasor create(Object context, Class<?> primarySource) {
        return configure(Hasor.create(context), primarySource);
    }

    public static AppContext run(Class<?> primarySource, String... args) {
        return create(primarySource).bindArguments(args).build();
    }

    public static AppContext run(Object context, Class<?> primarySource, String... args) {
        return create(context, primarySource).bindArguments(args).build();
    }

    private static Hasor configure(Hasor hasor, Class<?> primarySource) {
        Objects.requireNonNull(primarySource, "primarySource must not be null.");
        if (primarySource.getAnnotation(Configuration.class) == null) {
            throw new IllegalArgumentException(primarySource.getName() + " must be annotated with @Configuration.");
        }

        return hasor.addPrimarySources(primarySource);
    }
}
