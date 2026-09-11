/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.jetty;

import javax.servlet.ServletContext;
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Module;

/** Automatically starts embedded Jetty as part of the Hasor module lifecycle. */
public class JettyWebServerModule implements Module {
    private JettyWebServer server;

    @Override
    public void loadModule(ApiBinder apiBinder) {
        if (apiBinder.getContext() instanceof ServletContext) {
            throw new IgnoreModuleException();
        }
        WebServerConfig config = new WebServerConfig().loadSettings(apiBinder.getSettings());
        this.server = (JettyWebServer) WebServers.create(config);
        apiBinder.bindType(WebServer.class).toInstance(this.server);
        apiBinder.bindType(WebServer.class).nameWith("Jetty").toInstance(this.server);
    }

    @Override
    public void onStart(AppContext appContext) throws Throwable {
        this.server.start();
    }

    @Override
    public void onStop(AppContext appContext) throws Throwable {
        this.server.stop();
    }
}
