/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import net.hasor.boot.loader.jar.JarFile;

/**
 * Resource loader backed by a jar file and optional nested jar entries.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-19
 */
public class JarResourceLoader implements ResourceLoader {
    private final JarFile               jarFile;
    private final List<JarFile>         nestedJarFiles = new ArrayList<>();
    private final boolean               nestedMode;
    private final Map<String, Manifest> manifestCache  = new ConcurrentHashMap<>();

    public JarResourceLoader(File file) throws IOException {
        this(file, (Predicate<JarEntry>) null);
    }

    public JarResourceLoader(File file, Predicate<JarEntry> nestedPredicate) throws IOException {
        this.jarFile = new JarFile(file);
        this.nestedMode = nestedPredicate != null;
        if (nestedPredicate != null) {
            for (JarEntry jarEntry : this.jarFile) {
                if (jarEntry != null && nestedPredicate.test(jarEntry)) {
                    this.nestedJarFiles.add(this.jarFile.getNestedJarFile(jarEntry));
                }
            }
        }
    }

    public JarResourceLoader(File file, String nestedDirectory) throws IOException {
        this.jarFile = new JarFile(file);
        this.nestedMode = true;
        this.nestedJarFiles.add(this.jarFile.getNestedJarFile(nestedDirectory));
    }

    public Manifest getManifest() throws IOException {
        return this.jarFile.getManifest();
    }

    @Override
    public URL getResource(String resource) throws IOException {
        String resourceName = normalizeResource(resource);
        ZipEntry zipEntry = this.jarFile.getEntry(resourceName);
        if (zipEntry != null) {
            return new URL(this.jarFile.getUrl(), zipEntry.getName());
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            ZipEntry nestedZipEntry = nestedJar.getEntry(resourceName);
            if (nestedZipEntry != null) {
                return new URL(nestedJar.getUrl(), nestedZipEntry.getName());
            }
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String resource) throws IOException {
        String resourceName = normalizeResource(resource);
        ZipEntry zipEntry = this.jarFile.getEntry(resourceName);
        if (zipEntry != null) {
            return this.jarFile.getInputStream(zipEntry);
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            ZipEntry nestedZipEntry = nestedJar.getEntry(resourceName);
            if (nestedZipEntry != null) {
                return nestedJar.getInputStream(nestedZipEntry);
            }
        }
        return null;
    }

    @Override
    public long getResourceSize(String resource) throws IOException {
        String resourceName = normalizeResource(resource);
        ZipEntry zipEntry = this.jarFile.getEntry(resourceName);
        if (zipEntry != null) {
            return zipEntry.getSize();
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            ZipEntry nestedZipEntry = nestedJar.getEntry(resourceName);
            if (nestedZipEntry != null) {
                return nestedZipEntry.getSize();
            }
        }
        return -1;
    }

    @Override
    public List<URL> getResources(String resource) throws IOException {
        String resourceName = normalizeResource(resource);
        List<URL> result = new ArrayList<>();
        if (resourceName.isEmpty()) {
            if (!this.nestedMode) {
                result.add(this.jarFile.getUrl());
            }
            for (JarFile nestedJar : this.nestedJarFiles) {
                result.add(nestedJar.getUrl());
            }
            return result;
        }
        ZipEntry zipEntry = this.jarFile.getEntry(resourceName);
        if (zipEntry != null) {
            result.add(new URL(this.jarFile.getUrl(), zipEntry.getName()));
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            ZipEntry nestedZipEntry = nestedJar.getEntry(resourceName);
            if (nestedZipEntry != null) {
                result.add(new URL(nestedJar.getUrl(), nestedZipEntry.getName()));
            }
        }
        return result;
    }

    @Override
    public boolean exist(String resource) {
        String resourceName = normalizeResource(resource);
        if (this.jarFile.getEntry(resourceName) != null) {
            return true;
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            if (nestedJar.getEntry(resourceName) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Manifest getManifest(String resource) throws IOException {
        if (isEmpty(resource)) {
            return null;
        }
        URL url = getResource(resource);
        if (url == null) {
            return null;
        }
        String[] split = url.getPath().split("!/");
        if (split.length == 1) {
            return this.jarFile.getManifest();
        }
        String entryName = split[1];
        Manifest manifest = this.manifestCache.get(entryName);
        if (manifest != null) {
            return manifest;
        }
        for (JarFile nestedJar : this.nestedJarFiles) {
            String pathFromRoot = nestedJar.getPathFromRoot();
            if (isEmpty(pathFromRoot)) {
                continue;
            }
            if (pathFromRoot.startsWith("!/")) {
                pathFromRoot = pathFromRoot.substring(2);
            }
            this.manifestCache.computeIfAbsent(pathFromRoot, key -> {
                try {
                    return nestedJar.getManifest();
                } catch (IOException e) {
                    return null;
                }
            });
        }
        return this.manifestCache.get(entryName);
    }

    @Override
    public void close() throws IOException {
        IOException closeError = null;
        for (JarFile nestedJar : this.nestedJarFiles) {
            try {
                nestedJar.close();
            } catch (IOException e) {
                closeError = addCloseError(closeError, e);
            }
        }
        try {
            this.jarFile.close();
        } catch (IOException e) {
            closeError = addCloseError(closeError, e);
        } finally {
            this.manifestCache.clear();
        }
        if (closeError != null) {
            throw closeError;
        }
    }

    private static IOException addCloseError(IOException closeError, IOException error) {
        if (closeError == null) {
            return error;
        }
        closeError.addSuppressed(error);
        return closeError;
    }

    private static String normalizeResource(String resource) {
        if (resource == null) {
            return "";
        }
        resource = resource.replace('\\', '/');
        while (resource.startsWith("/")) {
            resource = resource.substring(1);
        }
        return resource;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
