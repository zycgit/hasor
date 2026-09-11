/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.async;
public abstract class AbstractAsyncAction {
    private final ThreadLocal<Object> data = new ThreadLocal<>();

    public ThreadLocal<Object> getData() {
        return data;
    }

    protected void initLocalObject() {
        if (this.data.get() != null) {
            this.data.remove();
        }
        this.data.set(new Object());
    }
}
