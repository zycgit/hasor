/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.spi;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class TestHttpRequestListener implements ServletRequestListener {
    private boolean requestDestroyed;
    private boolean requestInitialized;

    public boolean isRequestDestroyed() {
        return requestDestroyed;
    }

    public boolean isRequestInitialized() {
        return requestInitialized;
    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        this.requestDestroyed = true;
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        this.requestInitialized = true;
    }
}
