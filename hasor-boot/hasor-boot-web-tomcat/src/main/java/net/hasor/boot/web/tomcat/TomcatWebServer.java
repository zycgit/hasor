/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.tomcat;
import java.io.File;
import java.util.Map;
import javax.servlet.DispatcherType;
import net.hasor.boot.web.AbstractWebServer;
import net.hasor.boot.web.BootRuntimeListener;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.core.Module;
import net.hasor.web.startup.RuntimeFilter;
import net.hasor.web.startup.RuntimeListener;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * Embedded Tomcat server for Hasor Web MVC.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public class TomcatWebServer extends AbstractWebServer {
    private Tomcat tomcat;

    public TomcatWebServer(Class<? extends Module> rootModule) {
        super(rootModule);
    }

    public TomcatWebServer(WebServerConfig config) {
        super(config);
    }

    public static TomcatWebServer run(Class<? extends Module> rootModule) throws Exception {
        TomcatWebServer server = new TomcatWebServer(rootModule);
        server.start();
        return server;
    }

    @Override
    public void start() throws Exception {
        if (!markStarting()) {
            return;
        }
        Tomcat server = null;
        try {
            File documentRoot = getDocumentRootFile();
            server = new Tomcat();
            server.setHostname(this.config.getHost());
            server.setPort(this.config.getPort());
            server.setBaseDir(documentRoot.getAbsolutePath());
            server.getConnector();

            Context context = server.addContext(tomcatContextPath(), documentRoot.getAbsolutePath());
            context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
            context.addLifecycleListener(new Tomcat.FixContextListener());
            context.addApplicationListener(BootRuntimeListener.class.getName());
            for (Map.Entry<String, String> entry : getInitParameters().entrySet()) {
                context.addParameter(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Object> entry : this.config.getServletContextAttributes().entrySet()) {
                context.getServletContext().setAttribute(entry.getKey(), entry.getValue());
            }
            if (this.config.getAppContextFactory() != null) {
                context.getServletContext().setAttribute(RuntimeListener.AppContextFactoryName, this.config.getAppContextFactory());
            }
            addDefaultServlet(context);
            addHasorFilter(context);

            server.start();
            if (!context.getState().isAvailable()) {
                throw new IllegalStateException("Tomcat Web context failed to start.");
            }
            this.tomcat = server;
        } catch (Exception e) {
            destroy(server);
            markStartFailed();
            throw e;
        }
    }

    private String tomcatContextPath() {
        String contextPath = this.config.getContextPath();
        return "/".equals(contextPath) ? "" : contextPath;
    }

    private void addDefaultServlet(Context context) {
        Wrapper servlet = Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
        servlet.setLoadOnStartup(1);
        context.addServletMappingDecoded("/", "default");
    }

    private void addHasorFilter(Context context) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(this.config.getFilterName());
        filterDef.setFilterClass(RuntimeFilter.class.getName());
        filterDef.setAsyncSupported("true");
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(this.config.getFilterName());
        filterMap.addURLPattern(this.config.getFilterPattern());
        filterMap.setDispatcher(DispatcherType.REQUEST.name());
        filterMap.setDispatcher(DispatcherType.FORWARD.name());
        filterMap.setDispatcher(DispatcherType.INCLUDE.name());
        filterMap.setDispatcher(DispatcherType.ERROR.name());
        filterMap.setDispatcher(DispatcherType.ASYNC.name());
        context.addFilterMapBefore(filterMap);
    }

    private static void destroy(Tomcat server) {
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
        if (this.tomcat != null) {
            Tomcat server = this.tomcat;
            this.tomcat = null;
            destroy(server);
        }
    }

    @Override
    public int getPort() {
        if (this.tomcat != null && this.tomcat.getConnector() != null && this.tomcat.getConnector().getLocalPort() > 0) {
            return this.tomcat.getConnector().getLocalPort();
        }
        return super.getPort();
    }
}
