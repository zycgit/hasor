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

public class FirstTestExtension implements BootExtension {
    static final List<String> events = new ArrayList<>();

    public int order() {
        return -10;
    }

    public BootLauncher configure(BootConfiguration configuration, BootLauncher next) {
        return (environment, modules) -> {
            events.add("first-start");
            BootApplication application = next.start(environment, modules);
            application.onClose(() -> events.add("first-stop"));
            return application;
        };
    }
}
