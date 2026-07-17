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
package net.hasor.boot.web;
/**
 * Java SPI provider for embedded Hasor Web servers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-18
 */
public interface WebServerProvider {
    /** Server name used by {@code hasor.http.server}, for example {@code tomcat}. */
    String name();

    /** Create a server instance with the resolved configuration. */
    WebServer create(WebServerConfig config);
}
