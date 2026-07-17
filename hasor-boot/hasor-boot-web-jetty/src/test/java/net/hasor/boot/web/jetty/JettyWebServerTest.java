/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.boot.web.jetty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;

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
