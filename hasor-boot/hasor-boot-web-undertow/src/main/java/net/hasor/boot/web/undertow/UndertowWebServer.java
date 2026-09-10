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
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map;
import javax.servlet.DispatcherType;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import net.hasor.boot.web.AbstractWebServer;
import net.hasor.boot.web.BootRuntimeListener;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.core.Module;
import net.hasor.web.startup.RuntimeFilter;
import net.hasor.web.startup.RuntimeListener;

/**
 * Embedded Undertow server for Hasor Web MVC.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public class UndertowWebServer extends AbstractWebServer {
    private Undertow          server;
    private DeploymentManager deploymentManager;

    public UndertowWebServer(Class<? extends Module> rootModule) {
        super(rootModule);
    }

    public UndertowWebServer(WebServerConfig config) {
        super(config);
    }

    public static UndertowWebServer run(Class<? extends Module> rootModule) throws Exception {
        UndertowWebServer server = new UndertowWebServer(rootModule);
        server.start();
        return server;
    }

    @Override
    public void start() throws Exception {
        if (!markStarting()) {
            return;
        }
        DeploymentManager manager = null;
        Undertow undertow = null;
        try {
            File documentRoot = getDocumentRootFile();
            DeploymentInfo deploymentInfo = Servlets.deployment()//
                    .setClassLoader(Thread.currentThread().getContextClassLoader())//
                    .setContextPath(this.config.getContextPath())//
                    .setDeploymentName("hasor-web")//
                    .setResourceManager(new FileResourceManager(documentRoot, 1024))//
                    .addListener(Servlets.listener(BootRuntimeListener.class))//
                    .addFilter(Servlets.filter(this.config.getFilterName(), RuntimeFilter.class).setAsyncSupported(true));

            for (Map.Entry<String, String> entry : getInitParameters().entrySet()) {
                deploymentInfo.addInitParameter(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Object> entry : this.config.getServletContextAttributes().entrySet()) {
                deploymentInfo.addServletContextAttribute(entry.getKey(), entry.getValue());
            }
            if (this.config.getAppContextFactory() != null) {
                deploymentInfo.addServletContextAttribute(RuntimeListener.AppContextFactoryName, this.config.getAppContextFactory());
            }
            addHasorFilterMappings(deploymentInfo);

            manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
            manager.deploy();
            HttpHandler servletHandler = manager.start();
            undertow = Undertow.builder()//
                    .addHttpListener(this.config.getPort(), this.config.getHost())//
                    .setHandler(createPathHandler(servletHandler))//
                    .build();
            undertow.start();
            this.deploymentManager = manager;
            this.server = undertow;
        } catch (Exception e) {
            destroy(undertow, manager);
            markStartFailed();
            throw e;
        }
    }

    private void addHasorFilterMappings(DeploymentInfo deploymentInfo) {
        deploymentInfo.addFilterUrlMapping(this.config.getFilterName(), this.config.getFilterPattern(), DispatcherType.REQUEST);
        deploymentInfo.addFilterUrlMapping(this.config.getFilterName(), this.config.getFilterPattern(), DispatcherType.FORWARD);
        deploymentInfo.addFilterUrlMapping(this.config.getFilterName(), this.config.getFilterPattern(), DispatcherType.INCLUDE);
        deploymentInfo.addFilterUrlMapping(this.config.getFilterName(), this.config.getFilterPattern(), DispatcherType.ERROR);
        deploymentInfo.addFilterUrlMapping(this.config.getFilterName(), this.config.getFilterPattern(), DispatcherType.ASYNC);
    }

    private PathHandler createPathHandler(HttpHandler servletHandler) {
        String contextPath = this.config.getContextPath();
        PathHandler pathHandler = new PathHandler();
        if ("/".equals(contextPath)) {
            pathHandler.addPrefixPath("/", servletHandler);
        } else {
            pathHandler.addPrefixPath(contextPath, servletHandler);
        }
        return pathHandler;
    }

    private static void destroy(Undertow server, DeploymentManager deploymentManager) {
        if (server != null) {
            try {
                server.stop();
            } catch (Throwable e) {
                //
            }
        }
        if (deploymentManager != null) {
            try {
                deploymentManager.stop();
            } catch (Throwable e) {
                //
            }
            try {
                deploymentManager.undeploy();
            } catch (Throwable e) {
                //
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (!markStopping()) {
            return;
        }
        Undertow oldServer = this.server;
        DeploymentManager oldDeploymentManager = this.deploymentManager;
        this.server = null;
        this.deploymentManager = null;
        destroy(oldServer, oldDeploymentManager);
    }

    @Override
    public int getPort() {
        if (this.server != null) {
            for (Undertow.ListenerInfo listenerInfo : this.server.getListenerInfo()) {
                if (listenerInfo.getAddress() instanceof InetSocketAddress address && address.getPort() > 0) {
                    return address.getPort();
                }
            }
        }
        return super.getPort();
    }
}