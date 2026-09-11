/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.spi;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class TestHttpSessionListener implements HttpSessionListener {
    private boolean sessionCreated;
    private boolean sessionDestroyed;

    public boolean isSessionCreated() {
        return sessionCreated;
    }

    public boolean isSessionDestroyed() {
        return sessionDestroyed;
    }

    @Override
    public void sessionCreated(HttpSessionEvent sce) {
        this.sessionCreated = true;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sce) {
        this.sessionDestroyed = true;
    }
}
