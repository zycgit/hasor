package net.hasor.boot.web;

import net.hasor.config.web.WebDefaultsModule;
import net.hasor.core.Module;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

/** Adapts the Boot application source to container-independent Config defaults. */
final class BootWebModule implements WebModule {
    private final Module          application;
    private final WebServerConfig config;

    BootWebModule(Module application, WebServerConfig config) {
        this.application = application;
        this.config = config;
    }

    @Override
    public void loadModule(WebApiBinder binder) throws Throwable {
        if (this.application != null) {
            binder.installModule(this.application);
        }
        if (this.config == null) {
            return;
        }

        binder.installModule(new WebDefaultsModule(this.config.getWebOptions()));
    }
}
