/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core;
import java.lang.annotation.*;

/** Declares dependencies that must be obtained before this bean is created. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DependsOn {
    /** Exact binding IDs, including IDs assigned to configuration factory methods. */
    String[] value() default {};

    /** Registered types; each must resolve to exactly one binding. */
    Class<?>[] types() default {};
}
