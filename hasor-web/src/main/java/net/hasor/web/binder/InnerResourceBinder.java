/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.binder;
import java.util.Objects;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.loader.providers.MultiResourceLoader;

/** Configures one static resource URL mapping. */
class InnerResourceBinder implements ResourceBinder {
    private final ResourceLoader loader;
    private final String         pathPattern;
    private String               welcomeFile      = "index.html";
    private int                  order;
    private String[]             fallbackPaths    = new String[0];
    private String[]             excludedPrefixes = new String[0];
    private String               cacheControl     = "no-cache";

    public InnerResourceBinder(String pathPattern, ResourceLoader... loaders) {
        this.pathPattern = normalizePathPattern(pathPattern);
        if (loaders == null || loaders.length == 0) {
            throw new IllegalArgumentException("At least one resource loader is required.");
        }

        ResourceLoader[] copy = loaders.clone();
        for (ResourceLoader loader : copy) {
            Objects.requireNonNull(loader, "resource loader is null.");
        }

        this.loader = copy.length == 1 ? copy[0] : new MultiResourceLoader(copy);
    }

    @Override
    public ResourceBinder welcomeFile(String welcomeFile) {
        if (welcomeFile != null && (welcomeFile.contains("..") || welcomeFile.contains("\\") || welcomeFile.startsWith("/"))) {
            throw new IllegalArgumentException("Welcome file must be relative to the resource location");
        }

        this.welcomeFile = welcomeFile;
        return this;
    }

    @Override
    public ResourceBinder order(int order) {
        this.order = order;
        return this;
    }

    @Override
    public ResourceBinder fallbackPaths(String... paths) {
        this.fallbackPaths = paths.clone();
        return this;
    }

    @Override
    public ResourceBinder excludedPrefixes(String... prefixes) {
        this.excludedPrefixes = prefixes.clone();
        return this;
    }

    @Override
    public ResourceBinder cacheControl(String value) {
        if (value == null || value.contains("\r") || value.contains("\n")) {
            throw new IllegalArgumentException("Invalid Cache-Control value");
        }

        this.cacheControl = value;
        return this;
    }

    public int getOrder() {
        return this.order;
    }

    public int specificity() {
        return this.pathPattern.length();
    }

    public ResourceDef build() {
        return new ResourceDef(this.loader, this.pathPattern, //
                this.welcomeFile, this.fallbackPaths, this.excludedPrefixes, this.cacheControl);
    }

    private static String normalizePathPattern(String pathPattern) {
        if (StringUtils.isBlank(pathPattern)) {
            throw new IllegalArgumentException("pathPattern is blank.");
        }

        String normalized = pathPattern.trim().replace('\\', '/').replaceAll("/+", "/");
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.endsWith("/**")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        } else if (normalized.endsWith("/*")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }

        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.contains("*") || normalized.contains("?") || normalized.contains("..")) {
            throw new IllegalArgumentException("Resource mapping must be a path prefix with optional trailing /* or /**");
        }

        return normalized.isEmpty() ? "/" : normalized;
    }
}