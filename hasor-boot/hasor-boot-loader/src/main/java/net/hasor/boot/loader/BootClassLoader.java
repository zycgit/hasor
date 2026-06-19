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
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
/**
 * Application class loader for Hasor Boot executable jars.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-19
 */
public class BootClassLoader extends ClassLoader implements Closeable {
    private static final String[] PARENT_ONLY_PACKAGES  = new String[] { //
            "java.", //
            "net.hasor.boot.loader." //
    };
    private static final String[] PARENT_FIRST_PACKAGES = new String[] { //
            "jdk.", //
            "sun.", //
            "com.sun.", //
            "javax.crypto.", //
            "javax.imageio.", //
            "javax.management.", //
            "javax.naming.", //
            "javax.net.", //
            "javax.security.", //
            "javax.sound.", //
            "javax.sql.", //
            "javax.tools.", //
            "javax.transaction.xa.", //
            "javax.xml.", //
            "org.w3c.", //
            "org.xml." //
    };

    static {
        registerAsParallelCapable();
    }

    private final ResourceLoader resourceLoader;

    public BootClassLoader(ClassLoader parent, ResourceLoader resourceLoader) {
        super(parent);
        if (resourceLoader == null) {
            throw new IllegalArgumentException("resourceLoader is null.");
        }
        this.resourceLoader = resourceLoader;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                loaded = loadClassInternal(name);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    private Class<?> loadClassInternal(String name) throws ClassNotFoundException {
        if (isPackageMatch(name, PARENT_ONLY_PACKAGES)) {
            return loadClassFromParent(name);
        }
        if (isPackageMatch(name, PARENT_FIRST_PACKAGES)) {
            try {
                return loadClassFromParent(name);
            } catch (ClassNotFoundException ignored) {
                return findClass(name);
            }
        }
        try {
            return findClass(name);
        } catch (ClassNotFoundException ignored) {
            return loadClassFromParent(name);
        }
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        String resource = className.replace('.', '/') + ".class";
        try (InputStream inStream = this.resourceLoader.getResourceAsStream(resource)) {
            if (inStream == null) {
                throw new ClassNotFoundException(className);
            }
            int packageIndex = className.lastIndexOf('.');
            if (packageIndex != -1) {
                String packageName = className.substring(0, packageIndex);
                definePackageInternal(packageName, this.resourceLoader.getManifest(resource));
            }
            byte[] classBytes = readBytes(inStream);
            return defineClass(className, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        }
    }

    @Override
    public URL getResource(String resource) {
        URL url = findResource(resource);
        if (url != null) {
            return url;
        }
        ClassLoader parent = getParent();
        return parent == null ? ClassLoader.getSystemResource(resource) : parent.getResource(resource);
    }

    @Override
    protected URL findResource(String resource) {
        try {
            return this.resourceLoader.getResource(resource);
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(String resource) throws IOException {
        Set<URL> result = new LinkedHashSet<>();
        result.addAll(this.resourceLoader.getResources(resource));
        ClassLoader parent = getParent();
        Enumeration<URL> parentResources = parent == null ? ClassLoader.getSystemResources(resource) : parent.getResources(resource);
        while (parentResources.hasMoreElements()) {
            result.add(parentResources.nextElement());
        }
        return enumeration(result);
    }

    @Override
    public Enumeration<URL> findResources(String resource) throws IOException {
        return enumeration(this.resourceLoader.getResources(resource));
    }

    @Override
    public InputStream getResourceAsStream(String resource) {
        try {
            InputStream inputStream = this.resourceLoader.getResourceAsStream(resource);
            if (inputStream != null) {
                return inputStream;
            }
        } catch (IOException ignored) {
            return null;
        }
        ClassLoader parent = getParent();
        return parent == null ? ClassLoader.getSystemResourceAsStream(resource) : parent.getResourceAsStream(resource);
    }

    @Override
    public void close() throws IOException {
        this.resourceLoader.close();
    }

    private Class<?> loadClassFromParent(String name) throws ClassNotFoundException {
        ClassLoader parent = getParent();
        return parent == null ? findSystemClass(name) : parent.loadClass(name);
    }

    private static boolean isPackageMatch(String className, String[] packages) {
        for (String packageName : packages) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static Enumeration<URL> enumeration(Iterable<URL> iterable) {
        Iterator<URL> iterator = iterable.iterator();
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    private static Enumeration<URL> enumeration(List<URL> urls) {
        return enumeration((Iterable<URL>) new ArrayList<>(urls));
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int readLength;
        while ((readLength = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readLength);
        }
        return outputStream.toByteArray();
    }

    private void definePackageInternal(String packageName, Manifest manifest) {
        if (getDefinedPackage(packageName) != null) {
            return;
        }
        try {
            if (manifest != null) {
                definePackage(packageName, manifest);
            } else {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        } catch (IllegalArgumentException ignored) {
            if (getDefinedPackage(packageName) == null) {
                throw new AssertionError("Cannot find package " + packageName);
            }
        }
    }

    protected Package definePackage(String name, Manifest manifest) throws IllegalArgumentException {
        Attributes attributes = manifest.getMainAttributes();
        String specTitle = null;
        String specVersion = null;
        String specVendor = null;
        String implTitle = null;
        String implVersion = null;
        String implVendor = null;
        String sealed = null;
        URL sealBase = null;
        if (attributes != null) {
            specTitle = attributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
            specVersion = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
            specVendor = attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            implTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            implVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            implVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            sealed = attributes.getValue(Attributes.Name.SEALED);
        }
        return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, "true".equalsIgnoreCase(sealed) ? sealBase : null);
    }
}
