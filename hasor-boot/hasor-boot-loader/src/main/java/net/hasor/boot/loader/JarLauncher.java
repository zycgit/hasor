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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import net.hasor.cobble.loader.CobbleClassLoader;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.loader.jar.JarFile;
import net.hasor.cobble.loader.providers.JarResourceLoader;
import net.hasor.cobble.loader.providers.MultiResourceLoader;
import net.hasor.cobble.loader.providers.PrefixResourceLoader;

/**
 * Main entry for Hasor Boot executable jars.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
public class JarLauncher {
    public static void main(String[] args) throws Exception {
        new JarLauncher().launch(args);
    }

    protected void launch(String[] args) throws Exception {
        JarFile.registerUrlProtocolHandler();
        File archiveFile = getArchiveFile();
        JarResourceLoader archiveLoader = new JarResourceLoader(archiveFile);
        ResourceLoader appLoader = null;
        boolean completed = false;
        try {
            appLoader = createAppLoader(archiveFile, archiveLoader);
            String mainClass = getStartClass(archiveLoader.getManifest());
            CobbleClassLoader classLoader = appLoader.toClassLoader(getClass().getClassLoader());
            configureClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            invokeMain(classLoader, mainClass, args);
            registerCloseHook(classLoader, appLoader);
            completed = true;
        } finally {
            if (!completed && appLoader != null) {
                appLoader.close();
            } else if (!completed) {
                archiveLoader.close();
            }
        }
    }

    protected void registerCloseHook(ClassLoader classLoader, ResourceLoader appLoader) {
        try {
            Class<?> systemUtils = Class.forName("net.hasor.cobble.SystemUtils", true, classLoader);
            Method registerShutdownHook = systemUtils.getMethod("registerShutdownHook", Callable.class);
            Callable<?> closeHook = () -> {
                appLoader.close();
                return null;
            };
            registerShutdownHook.invoke(null, closeHook);
        } catch (Throwable ignored) {
            // If the application does not expose SystemUtils, the process exit will release jar handles.
        }
    }

    protected ResourceLoader createAppLoader(File archiveFile, JarResourceLoader archiveLoader) throws Exception {
        ResourceLoader classesLoader = new PrefixResourceLoader(archiveLoader, HasorBootLayout.APP_CLASSES);
        ResourceLoader libraryLoader = new JarResourceLoader(archiveFile, this::isNestedLibrary);
        return new MultiResourceLoader(classesLoader, libraryLoader);
    }

    protected boolean isNestedLibrary(JarEntry jarEntry) {
        String name = jarEntry.getName();
        return !jarEntry.isDirectory() && name.startsWith(HasorBootLayout.APP_LIB) && name.endsWith(".jar");
    }

    protected void configureClassLoader(CobbleClassLoader classLoader) {
        classLoader.addIncludePackages("*");
        classLoader.addExcludePackages("java.");
        classLoader.addExcludePackages("jdk.");
        classLoader.addExcludePackages("sun.");
        classLoader.addExcludePackages("com.sun.");
        classLoader.addExcludePackages("javax.xml.");
        classLoader.addExcludePackages("org.w3c.");
        classLoader.addExcludePackages("org.xml.");
        classLoader.addExcludePackages("net.hasor.cobble.loader.");
        classLoader.addExcludePackages("net.hasor.boot.loader.");
    }

    protected String getStartClass(Manifest manifest) {
        if (manifest == null) {
            throw new IllegalStateException("missing META-INF/MANIFEST.MF.");
        }
        Attributes attributes = manifest.getMainAttributes();
        String mainClass = attributes.getValue(HasorBootLayout.MANIFEST_START);
        if (mainClass == null || mainClass.trim().isEmpty()) {
            throw new IllegalStateException("missing manifest attribute " + HasorBootLayout.MANIFEST_START + ".");
        }
        return mainClass.trim();
    }

    protected void invokeMain(ClassLoader classLoader, String mainClass, String[] args) throws Exception {
        Class<?> startClass = Class.forName(mainClass, false, classLoader);
        Method main = startClass.getMethod("main", String[].class);
        try {
            main.invoke(null, new Object[] { args });
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                throw (Exception) target;
            }
            if (target instanceof Error) {
                throw (Error) target;
            }
            throw new IllegalStateException(target);
        }
    }

    protected File getArchiveFile() throws URISyntaxException {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException("cannot locate executable archive.");
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            throw new IllegalStateException("cannot locate executable archive.");
        }
        File file = new File(location.toURI());
        if (!file.isFile()) {
            throw new IllegalStateException("executable archive must be a jar file: " + file);
        }
        return file;
    }
}
