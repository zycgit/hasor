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
package net.hasor.boot.loader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;
/**
 * Presents a prefixed resource directory as a resource loader root.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-19
 */
public class PrefixResourceLoader implements ResourceLoader {
    private final ResourceLoader resourceLoader;
    private final String         prefix;

    public PrefixResourceLoader(ResourceLoader resourceLoader, String prefix) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("resourceLoader is null.");
        }
        this.resourceLoader = resourceLoader;
        this.prefix = formatPrefix(prefix);
    }

    @Override
    public URL getResource(String resource) throws IOException {
        return this.resourceLoader.getResource(withPrefix(resource));
    }

    @Override
    public InputStream getResourceAsStream(String resource) throws IOException {
        return this.resourceLoader.getResourceAsStream(withPrefix(resource));
    }

    @Override
    public long getResourceSize(String resource) throws IOException {
        return this.resourceLoader.getResourceSize(withPrefix(resource));
    }

    @Override
    public List<URL> getResources(String resource) throws IOException {
        return this.resourceLoader.getResources(withPrefix(resource));
    }

    @Override
    public boolean exist(String resource) {
        return this.resourceLoader.exist(withPrefix(resource));
    }

    @Override
    public Manifest getManifest(String resource) throws IOException {
        return this.resourceLoader.getManifest(withPrefix(resource));
    }

    @Override
    public void close() throws IOException {
        this.resourceLoader.close();
    }

    private String withPrefix(String resource) {
        if (resource == null || resource.isEmpty()) {
            return this.prefix;
        }
        resource = resource.replace('\\', '/');
        while (resource.startsWith("/")) {
            resource = resource.substring(1);
        }
        return this.prefix + resource;
    }

    private static String formatPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        prefix = prefix.replace('\\', '/');
        while (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix;
    }
}
