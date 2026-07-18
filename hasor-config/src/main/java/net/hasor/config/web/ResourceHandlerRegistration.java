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
package net.hasor.config.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.loader.providers.MultiResourceLoader;
import net.hasor.cobble.loader.providers.PathResourceLoader;
import net.hasor.cobble.loader.providers.PrefixResourceLoader;
import net.hasor.web.WebApiBinder;
import net.hasor.web.objects.ResourceFilter;

/** Configures one static resource URL mapping. */
public class ResourceHandlerRegistration {
    private final ResourceLoader       defaultLoader;
    private final String               pathPattern;
    private final List<ResourceLoader> resourceLoaders = new ArrayList<>();
    private       String               welcomeFile     = "index.html";
    private       int                  order;

    ResourceHandlerRegistration(ResourceLoader defaultLoader, String pathPattern) {
        this.defaultLoader = Objects.requireNonNull(defaultLoader, "defaultLoader is null.");
        this.pathPattern = normalizePathPattern(pathPattern);
    }

    /**
     * Adds resource locations. Locations beginning with {@code classpath:} use the application
     * resource loader; locations beginning with {@code file:} use a filesystem directory.
     */
    public ResourceHandlerRegistration addResourceLocations(String... locations) {
        if (locations != null) {
            for (String location : locations) {
                if (StringUtils.isBlank(location)) {
                    continue;
                }
                this.resourceLoaders.add(this.createLoader(location.trim()));
            }
        }
        return this;
    }

    /** Adds an application-supplied resource loader. */
    public ResourceHandlerRegistration addResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoaders.add(Objects.requireNonNull(resourceLoader, "resourceLoader is null."));
        return this;
    }

    public ResourceHandlerRegistration setWelcomeFile(String welcomeFile) {
        this.welcomeFile = welcomeFile;
        return this;
    }

    public ResourceHandlerRegistration setOrder(int order) {
        this.order = order;
        return this;
    }

    void register(WebApiBinder webBinder) {
        if (this.resourceLoaders.isEmpty()) {
            throw new IllegalStateException("Resource handler '" + this.pathPattern + "' requires at least one resource location.");
        }
        ResourceLoader loader = this.resourceLoaders.size() == 1 ? this.resourceLoaders.get(0) : new MultiResourceLoader(this.resourceLoaders.toArray(new ResourceLoader[0]));
        String urlPrefix = "/".equals(this.pathPattern) ? null : this.pathPattern;
        webBinder.filter("/*").through(this.order, new ResourceFilter(loader, urlPrefix, this.welcomeFile));
    }

    private ResourceLoader createLoader(String location) {
        if (location.startsWith("file:")) {
            return new PathResourceLoader(new File(location.substring("file:".length())));
        }
        String resourcePath = location.startsWith("classpath:") ? location.substring("classpath:".length()) : location;
        return new PrefixResourceLoader(this.defaultLoader, resourcePath);
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
        return normalized.isEmpty() ? "/" : normalized;
    }
}
