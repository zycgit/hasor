/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.Objects;
import net.hasor.web.ExceptionHandler;
import net.hasor.web.Invoker;

/** Binds an exception type to its handler without losing the handler's parameter type. */
public final class ExceptionDef<E extends Throwable> {
    private final Class<E>                    exceptionType;
    private final ExceptionHandler<? super E> handler;

    public ExceptionDef(Class<E> exceptionType, ExceptionHandler<? super E> handler) {
        this.exceptionType = Objects.requireNonNull(exceptionType);
        this.handler = Objects.requireNonNull(handler);
    }

    public Class<E> getExceptionType() {
        return this.exceptionType;
    }

    public Object handleException(Invoker invoker, Throwable exception) throws Throwable {
        return this.handler.handleException(invoker, this.exceptionType.cast(exception));
    }
}
