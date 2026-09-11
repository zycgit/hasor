/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.hasor.core.Module;

/**
 * Base implementation for embedded Hasor Web HTTP servers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public abstract class AbstractWebServer implements WebServer {
    protected final WebServerConfig config;
    private final   AtomicBoolean   started          = new AtomicBoolean(false);
    private final   Object          lifecycleMonitor = new Object();

    protected AbstractWebServer() {
        this(new WebServerConfig());
    }

    protected AbstractWebServer(Class<? extends Module> rootModule) {
        this(WebServerConfig.of(rootModule));
    }

    protected AbstractWebServer(WebServerConfig config) {
        this.config = config == null ? new WebServerConfig() : config.copy();
    }

    protected boolean markStarting() {
        return this.started.compareAndSet(false, true);
    }

    protected void markStartFailed() {
        synchronized (this.lifecycleMonitor) {
            this.started.set(false);
            this.lifecycleMonitor.notifyAll();
        }
    }

    protected boolean markStopping() {
        synchronized (this.lifecycleMonitor) {
            if (!this.started.compareAndSet(true, false)) {
                return false;
            }
            this.lifecycleMonitor.notifyAll();
            return true;
        }
    }

    protected File getDocumentRootFile() throws IOException {
        File documentRoot = this.config.getDocumentRoot();
        if (!documentRoot.exists() && !documentRoot.mkdirs()) {
            throw new IOException("Cannot create documentRoot " + documentRoot);
        }
        if (!documentRoot.isDirectory()) {
            throw new IOException("documentRoot is not a directory " + documentRoot);
        }
        return documentRoot.getCanonicalFile();
    }

    protected Map<String, String> getInitParameters() {
        return this.config.getInitParameters();
    }

    @Override
    public boolean isStart() {
        return this.started.get();
    }

    @Override
    public void join() throws InterruptedException {
        synchronized (this.lifecycleMonitor) {
            while (isStart()) {
                this.lifecycleMonitor.wait();
            }
        }
    }

    @Override
    public String getHost() {
        return this.config.getHost();
    }

    @Override
    public int getPort() {
        return this.config.getPort();
    }

    @Override
    public String getContextPath() {
        return this.config.getContextPath();
    }
}
