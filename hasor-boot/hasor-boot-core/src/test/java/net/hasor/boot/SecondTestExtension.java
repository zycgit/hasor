/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
public class SecondTestExtension implements BootExtension {
    public BootLauncher configure(BootConfiguration configuration, BootLauncher next) {
        return (environment, modules) -> {
            FirstTestExtension.events.add("second-start");
            BootApplication application = next.start(environment, modules);
            application.onClose(() -> FirstTestExtension.events.add("second-stop"));
            return application;
        };
    }
}
