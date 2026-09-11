/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;

/** Configures the Web policy of a URL binding backed by one or more resource loaders. */
public interface ResourceBinder {
    /** Relative welcome file; null disables it. */
    ResourceBinder welcomeFile(String welcomeFile);

    /** Explicit request paths eligible for SPA fallback. */
    ResourceBinder fallbackPaths(String... paths);

    ResourceBinder cacheControl(String cacheControl);

    ResourceBinder excludedPrefixes(String... prefixes);

    /** Lower values take priority among resource bindings; Actions always win. */
    ResourceBinder order(int order);
}
