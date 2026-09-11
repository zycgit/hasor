/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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

/**
 * Factory methods for embedded Hasor Web servers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public final class WebServers {
    private static final Logger logger = LoggerFactory.getLogger(WebServers.class);

    private WebServers() {
    }

    public static WebServer run(String[] args, Class<?> rootModule) throws Exception {
        return run(create(args, rootModule));
    }

    public static WebServer run(WebServerConfig config) throws Exception {
        return run(create(config));
    }

    private static WebServer run(WebServer server) throws Exception {
        AutoCloseable shutdownHook = SystemUtils.registerShutdownHook(() -> {
            if (server.isStart()) {
                server.stop();
            }
            return null;
        });
        try {
            server.start();
            if (server.getPort() >= 0) {
                logger.info("Hasor Web started at " + serverUrl(server));
            } else {
                logger.info("Hasor Web started without an HTTP listener.");
            }
            return server;
        } catch (Exception e) {
            shutdownHook.close();
            throw e;
        }
    }

    public static WebServer create(String[] args, Class<?> rootModule) {
        Settings settings = Hasor.create().buildSettings();
        WebServerConfig config = WebServerConfig.of(settings, rootModule).arguments(args);
        return create(config);
    }

    public static WebServer create(WebServerConfig config) {
        WebServerConfig copy = config == null ? new WebServerConfig() : config.copy();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            WebServerProvider provider = findProvider(classLoader);
            return provider.create(copy);
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private static WebServerProvider findProvider(ClassLoader classLoader) {
        List<String> names = new ArrayList<>();
        WebServerProvider candidate = null;
        for (WebServerProvider provider : ServiceLoader.load(WebServerProvider.class, classLoader)) {
            String providerName = provider.name();
            if (StringUtils.isBlank(providerName)) {
                throw new IllegalStateException(provider.getClass().getName() + " provider name is blank.");
            }
            names.add(providerName);
            candidate = provider;
        }
        if (names.size() == 1) {
            return candidate;
        }
        if (names.size() > 1) {
            throw new IllegalStateException("Multiple embedded WebServerProviders found: " + names
                    + ". Keep only one embedded container dependency.");
        }
        throw new IllegalStateException("No embedded WebServerProvider found. Add hasor-boot-web-tomcat, hasor-boot-web-jetty, or hasor-boot-web-undertow.");
    }

    private static String serverUrl(WebServer server) {
        String host = "0.0.0.0".equals(server.getHost()) ? "localhost" : server.getHost();
        return "http://" + host + ":" + server.getPort() + WebServerConfig.normalizeContextPath(server.getContextPath());
    }
}
