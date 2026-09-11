/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.tomcat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;
import org.junit.Test;
import static org.junit.Assert.*;

public class TomcatWebServerTest {
    @Test
    public void jarShouldAutoStartAndStopWithHasor() {
        AppContext appContext = Hasor.create()//
                .addSettings(Settings.DefaultNameSpace, "hasor.boot.web.connectors.http.port", 0)//
                .build();
        WebServer server = appContext.getInstance(WebServer.class);

        assertTrue(server instanceof TomcatWebServer);
        assertSame(server, appContext.findBindingBean("Tomcat", WebServer.class));
        assertTrue(server.isStart());
        assertTrue(server.getPort() > 0);

        appContext.shutdown();
        assertFalse(server.isStart());
    }

    @Test
    public void startStop() throws Exception {
        AppContext[] holder = new AppContext[1];
        WebServerConfig config = new WebServerConfig().port(0).appContextFactory(servletContext -> {
            holder[0] = Hasor.create(servletContext).build(new StartModule());
            return holder[0];
        });
        WebServer server = WebServers.create(config);
        assertTrue(server instanceof TomcatWebServer);
        try {
            server.start();
            assertTrue(server.isStart());
            assertTrue(server.getPort() > 0);
            assertEquals(Integer.toString(System.identityHashCode(holder[0])), get("http://127.0.0.1:" + server.getPort() + "/context-id"));
        } finally {
            server.stop();
        }
        assertFalse(server.isStart());
    }

    private static String get(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        try (InputStream input = connection.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    public static class StartModule implements WebModule {
        @Override
        public void loadModule(WebApiBinder apiBinder) {
            apiBinder.loadMappingTo(ContextController.class);
        }
    }

    @MappingTo("/context-id")
    public static class ContextController {
        @Get
        public void contextId(AppContext appContext, HttpServletResponse response) throws Exception {
            response.getWriter().write(Integer.toString(System.identityHashCode(appContext)));
        }
    }
}
