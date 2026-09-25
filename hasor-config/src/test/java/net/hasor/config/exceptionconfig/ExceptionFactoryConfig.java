/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.exceptionconfig;

import java.io.IOException;
import net.hasor.config.Configuration;
import net.hasor.config.web.Exception;
import net.hasor.web.ExceptionHandler;

@Configuration
public class ExceptionFactoryConfig {
    @Exception(IOException.class)
    public ExceptionHandler<IOException> failures() {
        return (invoker, error) -> "scanned:" + error.getMessage();
    }
}
