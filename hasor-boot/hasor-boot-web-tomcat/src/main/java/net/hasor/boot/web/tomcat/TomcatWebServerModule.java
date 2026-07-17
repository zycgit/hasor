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
package net.hasor.boot.web.tomcat;

import javax.servlet.ServletContext;
import net.hasor.cobble.StringUtils;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Module;
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;

/** Automatically starts embedded Tomcat as part of the Hasor module lifecycle. */
public class TomcatWebServerModule implements Module {
    private TomcatWebServer server;

    @Override
    public void loadModule(ApiBinder apiBinder) {
        if (apiBinder.getContext() instanceof ServletContext) {
            throw new IgnoreModuleException();
        }
        String serverName = apiBinder.getSettings().getString("hasor.http.server", null);
        if (StringUtils.isNotBlank(serverName) && !"Tomcat".equalsIgnoreCase(serverName)) {
            throw new IgnoreModuleException();
        }

        WebServerConfig config = new WebServerConfig().loadSettings(apiBinder.getSettings());
        this.server = new TomcatWebServer(config);
        apiBinder.bindType(WebServer.class).toInstance(this.server);
        apiBinder.bindType(WebServer.class).nameWith("Tomcat").toInstance(this.server);
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
