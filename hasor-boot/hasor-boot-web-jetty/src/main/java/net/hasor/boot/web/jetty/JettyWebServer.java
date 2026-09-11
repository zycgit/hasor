/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.jetty;
import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import net.hasor.boot.web.AbstractWebServer;
import net.hasor.boot.web.BootRuntimeListener;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.core.Module;
import net.hasor.web.startup.RuntimeFilter;
import net.hasor.web.startup.RuntimeListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Embedded Jetty server for Hasor Web MVC.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public class JettyWebServer extends AbstractWebServer {
    private Server          server;
    private ServerConnector connector;

    public JettyWebServer(Class<? extends Module> rootModule) {
        super(rootModule);
    }

    public JettyWebServer(WebServerConfig config) {
        super(config);
    }

    public static JettyWebServer run(Class<? extends Module> rootModule) throws Exception {
        JettyWebServer server = new JettyWebServer(rootModule);
        server.start();
        return server;
    }

    @Override
    public void start() throws Exception {
        if (!markStarting()) {
            return;
        }
        Server newServer = null;
        try {
            File documentRoot = getDocumentRootFile();
            newServer = new Server();
            ServerConnector newConnector = null;
            if (this.config.isHttpEnabled()) {
                newConnector = new ServerConnector(newServer);
                newConnector.setHost(this.config.getHost());
                newConnector.setPort(this.config.getPort());
                newServer.addConnector(newConnector);
            }

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath(this.config.getContextPath());
            context.setResourceBase(documentRoot.getAbsolutePath());
            context.addEventListener(new BootRuntimeListener());
            for (Map.Entry<String, String> entry : getInitParameters().entrySet()) {
                context.setInitParameter(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Object> entry : this.config.getServletContextAttributes().entrySet()) {
                context.setAttribute(entry.getKey(), entry.getValue());
            }
            if (this.config.getAppContextFactory() != null) {
                context.setAttribute(RuntimeListener.AppContextFactoryName, this.config.getAppContextFactory());
            }

            FilterHolder filterHolder = new FilterHolder(RuntimeFilter.class);
            filterHolder.setName(this.config.getFilterName());
            context.addFilter(filterHolder, this.config.getFilterPattern(), allDispatcherTypes());

            newServer.setHandler(context);
            newServer.start();
            this.server = newServer;
            this.connector = newConnector;
        } catch (Exception e) {
            destroy(newServer);
            markStartFailed();
            throw e;
        }
    }

    private static EnumSet<DispatcherType> allDispatcherTypes() {
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR, DispatcherType.ASYNC);
    }

    private static void destroy(Server server) {
        if (server == null) {
            return;
        }
        try {
            server.stop();
        } catch (Throwable e) {
            //
        }
        try {
            server.destroy();
        } catch (Throwable e) {
            //
        }
    }

    @Override
    public void stop() throws Exception {
        if (!markStopping()) {
            return;
        }
        if (this.server != null) {
            Server oldServer = this.server;
            this.server = null;
            this.connector = null;
            destroy(oldServer);
        }
    }

    @Override
    public int getPort() {
        if (this.connector != null && this.connector.getLocalPort() > 0) {
            return this.connector.getLocalPort();
        }
        return super.getPort();
    }
}
