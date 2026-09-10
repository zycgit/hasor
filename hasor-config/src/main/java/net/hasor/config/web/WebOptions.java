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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Container-independent Web defaults, shared by standalone Config and Boot. */
public final class WebOptions {
    private boolean           staticResources  = true;
    private final Set<String> scanExcludes     = new LinkedHashSet<>();
    private String            staticLocation   = "META-INF/resources";
    private String[]          spaPaths         = new String[0];
    private String[]          resourceExcludes = { "/api", "/health" };
    private String[]          noStorePaths     = new String[0];

    public WebOptions() {
    }

    public WebOptions(WebOptions source) {
        this.staticResources = source.staticResources;
        this.scanExcludes.addAll(source.scanExcludes);
        this.staticLocation = source.staticLocation;
        this.spaPaths = source.spaPaths.clone();
        this.resourceExcludes = source.resourceExcludes.clone();
        this.noStorePaths = source.noStorePaths.clone();
    }

    public WebOptions copy() {
        return new WebOptions(this);
    }

    public String[] getNoStorePaths() {
        return this.noStorePaths.clone();
    }

    public WebOptions noStorePaths(String... paths) {
        for (String path : paths) {
            if (path == null || !path.startsWith("/") || path.contains("..") || path.contains("\\")) {
                throw new IllegalArgumentException("Invalid response policy path: " + path);
            }
        }

        this.noStorePaths = paths.clone();
        return this;
    }

    public Set<String> getScanExcludes() {
        return Collections.unmodifiableSet(this.scanExcludes);
    }

    public WebOptions excludeScanNames(String... names) {
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                this.scanExcludes.add(name);
            }
        }
        return this;
    }

    public WebOptions excludeScan(Class<?>... types) {
        for (Class<?> type : types) {
            this.scanExcludes.add(type.getName());
        }

        return this;
    }

    public boolean isStaticResources() {
        return this.staticResources;
    }

    public WebOptions staticResources(boolean enabled) {
        this.staticResources = enabled;
        return this;
    }

    public String getStaticLocation() {
        return this.staticLocation;
    }

    public WebOptions staticLocation(String location) {
        if (location == null || !location.matches("[\\w-]+(/[\\w-]+)*")) {
            throw new IllegalArgumentException("Static location must be a bounded classpath directory, without leading/trailing slash.");
        }

        this.staticLocation = location;
        return this;
    }

    public String[] getSpaPaths() {
        return this.spaPaths.clone();
    }

    public WebOptions spaPaths(String... paths) {
        for (String path : paths) {
            if (path == null || !path.startsWith("/") || path.contains("..") || path.contains("\\")) {
                throw new IllegalArgumentException("SPA paths must be absolute URL paths.");
            }
        }

        this.spaPaths = paths.clone();
        return this;
    }

    public String[] getResourceExcludes() {
        return this.resourceExcludes.clone();
    }

    public WebOptions resourceExcludes(String... prefixes) {
        for (String prefix : prefixes) {
            if (prefix == null || !prefix.startsWith("/") || prefix.endsWith("/")) {
                throw new IllegalArgumentException("Resource exclusions must be URL prefixes without trailing slash.");
            }
        }

        this.resourceExcludes = prefixes.clone();
        return this;
    }
}
