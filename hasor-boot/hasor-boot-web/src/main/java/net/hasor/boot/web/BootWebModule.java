/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
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
        binder.installModule(new net.hasor.boot.web.health.HealthModule());
    }
}
