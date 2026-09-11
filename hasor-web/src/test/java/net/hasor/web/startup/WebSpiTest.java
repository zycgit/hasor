/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.startup;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class WebSpiTest implements Module {
    public static Set<String> spiCall = new HashSet<>();

    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindSpiListener(ServletContextListener.class, new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                spiCall.add("ServletContextListener.contextInitialized");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                spiCall.add("ServletContextListener.contextDestroyed");
            }
        });
        //
        apiBinder.bindSpiListener(HttpSessionListener.class, new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent sce) {
                spiCall.add("HttpSessionListener.sessionCreated");
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent sce) {
                spiCall.add("HttpSessionListener.sessionDestroyed");
            }
        });
        //
        apiBinder.bindSpiListener(ServletRequestListener.class, new ServletRequestListener() {
            @Override
            public void requestInitialized(ServletRequestEvent sce) {
                spiCall.add("ServletRequestListener.requestInitialized");
            }

            @Override
            public void requestDestroyed(ServletRequestEvent sce) {
                spiCall.add("ServletRequestListener.requestDestroyed");
            }
        });
        //
    }
}
