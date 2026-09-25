/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;

/** Handles a registered exception type from a mapped MVC request. */
@FunctionalInterface
public interface ExceptionHandler<E extends Throwable> {
    /**
     * Returns response data, or null to propagate the original exception.
     * A handler that writes its own response must still return a non-null value, such as an empty string.
     */
    Object handleException(Invoker invoker, E exception) throws Throwable;
}
