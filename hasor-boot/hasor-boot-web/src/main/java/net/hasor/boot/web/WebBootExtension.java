/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.hasor.boot.BootApplication;
import net.hasor.boot.BootConfiguration;
import net.hasor.boot.BootExtension;
import net.hasor.boot.BootLauncher;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.config.web.WebDefaultsModule;
import net.hasor.config.web.WebOptions;
import net.hasor.core.Module;

/** Web 启动扩展：准备 Servlet 环境，再委托统一启动链创建应用。 */
public final class WebBootExtension implements BootExtension {
    @Override
    public BootLauncher configure(BootConfiguration configuration, BootLauncher next) {
        return (environment, modules) -> {
            if (environment != null) {
                throw new IllegalStateException("Web requires ownership of the application environment.");
            }
            AtomicReference<BootApplication> application = new AtomicReference<>();
            AtomicReference<WebServer> serverRef = new AtomicReference<>();

            WebServerConfig config = new WebServerConfig()    //
                    .loadSettings(configuration.getSettings())//
                    .arguments(configuration.getArguments());

            config.appContextFactory(sc -> {
                sc.setAttribute(WebOptions.class.getName(), config.getWebOptions());
                List<Module> assembled = new ArrayList<>(modules);
                assembled.add(b -> b.bindType(WebServer.class).toInstance(serverRef.get()));
                assembled.add(new WebDefaultsModule(config.getWebOptions()));
                assembled.add(new net.hasor.boot.web.health.HealthModule());

                try {
                    BootApplication started = next.start(sc, assembled);
                    if (!application.compareAndSet(null, started)) {
                        started.close();
                        throw new IllegalStateException("Application context has already been created.");
                    }
                    return started.getAppContext();
                } catch (Exception e) {
                    throw ExceptionUtils.toRuntime(e);
                }
            });

            WebServer server = WebServers.create(config);
            serverRef.set(server);
            try {
                server.start();
                BootApplication started = application.get();
                if (started == null) {
                    throw new IllegalStateException("Web server did not create an application context.");
                }
                started.onClose(server);
                return started;
            } catch (Exception | Error e) {
                try {
                    server.stop();
                } catch (Throwable cleanup) {
                    e.addSuppressed(cleanup);
                }
                if (application.get() != null) {
                    try {
                        application.get().close();
                    } catch (Throwable cleanup) {
                        e.addSuppressed(cleanup);
                    }
                }
                throw e;
            }
        };
    }
}
