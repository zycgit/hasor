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
import net.hasor.boot.web.WebServerProvider;

/**
 * Java SPI provider for embedded Jetty.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-18
 */
public class JettyWebServerProvider implements WebServerProvider {
    @Override
    public String name() {
        return "Jetty";
    }

    @Override
    public WebServer create(WebServerConfig config) {
        return new JettyWebServer(config);
    }
}
