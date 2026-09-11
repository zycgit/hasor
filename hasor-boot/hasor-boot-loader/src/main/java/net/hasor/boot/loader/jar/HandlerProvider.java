/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

/**
 * {@link URLStreamHandler} for loader {@link JarFile}s.
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @see JarFile#registerUrlProtocolHandler()
 * @since 1.0.0
 */
public class HandlerProvider extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("jar".equalsIgnoreCase(protocol)) {
            return new Handler();
        } else {
            return null;
        }
    }
}
