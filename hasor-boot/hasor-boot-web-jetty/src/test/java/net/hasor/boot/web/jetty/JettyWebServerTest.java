/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.jetty;
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import org.junit.Test;
import static org.junit.Assert.*;

public class JettyWebServerTest {
    @Test
    public void jarShouldAutoStartAndStopWithHasor() {
        AppContext appContext = Hasor.create()//
                .addSettings(Settings.DefaultNameSpace, "hasor.http.port", 0)//
                .build();
        WebServer server = appContext.getInstance(WebServer.class);

        assertTrue(server instanceof JettyWebServer);
        assertSame(server, appContext.findBindingBean("Jetty", WebServer.class));
        assertTrue(server.isStart());
        assertTrue(server.getPort() > 0);

        appContext.shutdown();
        assertFalse(server.isStart());
    }

    @Test
    public void startStop() throws Exception {
        WebServer server = WebServers.create(WebServerConfig.of(StartModule.class).server("jetty").port(0));
        assertTrue(server instanceof JettyWebServer);
        try {
            server.start();
            assertTrue(server.isStart());
            assertTrue(server.getPort() > 0);
        } finally {
            server.stop();
        }
        assertFalse(server.isStart());
    }

    public static class StartModule implements WebModule {
        @Override
        public void loadModule(WebApiBinder apiBinder) {
            //
        }
    }
}
