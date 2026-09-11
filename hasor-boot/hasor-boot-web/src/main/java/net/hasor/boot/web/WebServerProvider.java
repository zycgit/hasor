/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
/**
 * Java SPI provider for embedded Hasor Web servers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-18
 */
public interface WebServerProvider {
    /** Server name used in diagnostics, for example {@code tomcat}. */
    String name();

    /** Create a server instance with the resolved configuration. */
    WebServer create(WebServerConfig config);
}
