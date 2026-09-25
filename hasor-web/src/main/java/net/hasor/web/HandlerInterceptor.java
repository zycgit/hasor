/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;

/** Intercepts a matched MVC invocation, independently of the outer HTTP filters. */
public interface HandlerInterceptor {
    /** Return false when the request has been handled and MVC execution must stop. */
    default boolean preHandle(Invoker invoker) throws Throwable {
        return true;
    }

    /** Runs in reverse order after a successful controller call, before rendering. */
    default void postHandle(Invoker invoker, Object result) throws Throwable {
    }

    /**
     * Runs in reverse order for interceptors whose preHandle returned true.
     * The failure is null after normal completion or a resolved exception.
     */
    default void afterCompletion(Invoker invoker, Throwable failure) throws Throwable {
    }
}
