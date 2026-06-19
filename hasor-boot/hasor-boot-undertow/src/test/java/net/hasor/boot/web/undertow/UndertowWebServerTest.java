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
package net.hasor.boot.web.undertow;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.http.WebServer;
import net.hasor.web.http.WebServerConfig;
import net.hasor.web.http.WebServers;

public class UndertowWebServerTest {
    @Test
    public void startStop() throws Exception {
        WebServer server = WebServers.create(WebServerConfig.of(StartModule.class).server("undertow").port(0));
        assertTrue(server instanceof UndertowWebServer);
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
