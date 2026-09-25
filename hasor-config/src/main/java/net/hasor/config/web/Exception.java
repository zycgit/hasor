/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web;
import java.lang.annotation.*;

/** Registers an exception-handler factory declared in a configuration class. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Exception {
    /** Exception types handled by the factory's result. */
    Class<? extends Throwable>[] value();
}