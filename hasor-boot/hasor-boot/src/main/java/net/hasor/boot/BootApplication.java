/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.hasor.core.AppContext;

/** 已启动应用及其扩展资源；关闭时先释放扩展，再关闭应用容器。 */
public final class BootApplication implements AutoCloseable {
    private final AppContext          appContext;
    private final List<AutoCloseable> resources = new ArrayList<>();
    private       boolean             closed;

    public BootApplication(AppContext appContext) {
        this.appContext = Objects.requireNonNull(appContext, "appContext");
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public synchronized void onClose(AutoCloseable resource) {
        if (closed) {
            throw new IllegalStateException("Application is closed.");
        }
        resources.add(Objects.requireNonNull(resource, "resource"));
    }

    public void join() throws InterruptedException {
        appContext.join();
    }

    @Override
    public synchronized void close() throws Exception {
        if (closed) {
            return;
        }
        closed = true;
        Throwable failure = null;
        for (int i = resources.size() - 1; i >= 0; i--) {
            try {
                resources.get(i).close();
            } catch (Throwable e) {
                if (failure == null)
                    failure = e;
                else
                    failure.addSuppressed(e);
            }
        }
        resources.clear();
        try {
            if (appContext.isStart())
                appContext.shutdown();
        } catch (Throwable e) {
            if (failure == null)
                failure = e;
            else
                failure.addSuppressed(e);
        }
        if (failure instanceof Error error)
            throw error;
        if (failure instanceof Exception exception)
            throw exception;
    }
}
