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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.SystemUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.Hasor;
import net.hasor.core.Module;
/**
 * Factory methods for embedded Hasor Web servers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public final class WebServers {
    private static final Logger logger = LoggerFactory.getLogger(WebServers.class);

    private WebServers() {
    }

    public static WebServer run(String[] args, Class<? extends Module> rootModule) throws Exception {
        WebServer server = create(args, rootModule);
        AutoCloseable shutdownHook = SystemUtils.registerShutdownHook(() -> {
            if (server.isStart()) {
                server.stop();
            }
            return null;
        });
        try {
            server.start();
            logger.info("Hasor Web started at " + serverUrl(server));
            return server;
        } catch (Exception e) {
            shutdownHook.close();
            throw e;
        }
    }

    public static WebServer create(String[] args, Class<? extends Module> rootModule) {
        Settings settings = Hasor.create().buildSettings();
        WebServerConfig config = WebServerConfig.of(settings, rootModule).arguments(args);
        return create(config);
    }

    public static WebServer create(WebServerConfig config) {
        WebServerConfig copy = config == null ? new WebServerConfig() : config.copy();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            WebServerProvider provider = findProvider(classLoader, copy.getServer());
            copy.server(provider.name());
            return provider.create(copy);
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private static WebServerProvider findProvider(ClassLoader classLoader, String serverName) {
        List<String> names = new ArrayList<>();
        for (WebServerProvider provider : ServiceLoader.load(WebServerProvider.class, classLoader)) {
            String providerName = provider.name();
            if (StringUtils.isBlank(providerName)) {
                throw new IllegalStateException(provider.getClass().getName() + " provider name is blank.");
            }
            names.add(providerName);
            if (StringUtils.isBlank(serverName) || providerName.equalsIgnoreCase(serverName)) {
                return provider;
            }
        }
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalStateException("No embedded WebServerProvider found. Add hasor-boot-web-tomcat, hasor-boot-web-jetty, or hasor-boot-web-undertow.");
        } else {
            throw new IllegalStateException("No embedded WebServerProvider named '" + serverName + "' found. Available providers: " + names + ".");
        }
    }

    private static String serverUrl(WebServer server) {
        String host = "0.0.0.0".equals(server.getHost()) ? "localhost" : server.getHost();
        return "http://" + host + ":" + server.getPort() + WebServerConfig.normalizeContextPath(server.getContextPath());
    }
}
