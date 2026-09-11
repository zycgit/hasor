/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.spi;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TestServletContextListener implements ServletContextListener {
    private boolean contextInitialized;
    private boolean contextDestroyed;

    public boolean isContextInitialized() {
        return contextInitialized;
    }

    public boolean isContextDestroyed() {
        return contextDestroyed;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        contextInitialized = true;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        contextDestroyed = true;
    }
}
