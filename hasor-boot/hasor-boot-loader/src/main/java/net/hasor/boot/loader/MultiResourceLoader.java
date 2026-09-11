/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.Manifest;

/**
 * Composite resource loader used by the boot application classpath.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-19
 */
public class MultiResourceLoader implements ResourceLoader {
    private final List<ResourceLoader> loaders = new CopyOnWriteArrayList<>();

    public MultiResourceLoader(ResourceLoader... loaders) {
        if (loaders == null) {
            return;
        }
        for (ResourceLoader loader : loaders) {
            addLoader(loader);
        }
    }

    public void addLoader(ResourceLoader loader) {
        if (loader != null && !this.loaders.contains(loader)) {
            this.loaders.add(loader);
        }
    }

    public ResourceLoader findLoader(String resource) {
        for (ResourceLoader loader : this.loaders) {
            if (loader.exist(resource)) {
                return loader;
            }
        }
        return null;
    }

    @Override
    public URL getResource(String resource) throws IOException {
        ResourceLoader loader = findLoader(resource);
        return loader == null ? null : loader.getResource(resource);
    }

    @Override
    public InputStream getResourceAsStream(String resource) throws IOException {
        ResourceLoader loader = findLoader(resource);
        return loader == null ? null : loader.getResourceAsStream(resource);
    }

    @Override
    public long getResourceSize(String resource) throws IOException {
        ResourceLoader loader = findLoader(resource);
        return loader == null ? -1 : loader.getResourceSize(resource);
    }

    @Override
    public List<URL> getResources(String resource) throws IOException {
        List<URL> result = new ArrayList<>();
        for (ResourceLoader loader : this.loaders) {
            result.addAll(loader.getResources(resource));
        }
        return result;
    }

    @Override
    public boolean exist(String resource) {
        return findLoader(resource) != null;
    }

    @Override
    public Manifest getManifest(String resource) throws IOException {
        ResourceLoader loader = findLoader(resource);
        return loader == null ? null : loader.getManifest(resource);
    }

    @Override
    public void close() throws IOException {
        IOException closeError = null;
        for (ResourceLoader loader : this.loaders) {
            try {
                loader.close();
            } catch (IOException e) {
                if (closeError == null) {
                    closeError = e;
                } else {
                    closeError.addSuppressed(e);
                }
            }
        }
        if (closeError != null) {
            throw closeError;
        }
    }
}
