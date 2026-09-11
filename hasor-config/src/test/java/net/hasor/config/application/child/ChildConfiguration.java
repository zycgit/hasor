/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.application.child;

import net.hasor.config.Bean;
import net.hasor.config.Configuration;

@Configuration
public class ChildConfiguration {
    @Bean
    public ScannedService scannedService() {
        return new ScannedService("application-package");
    }
}
