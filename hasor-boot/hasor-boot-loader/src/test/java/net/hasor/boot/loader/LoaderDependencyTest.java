/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader;
import java.io.Closeable;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoaderDependencyTest {
    @Test
    public void loaderContainsOnlyItsRequiredCobbleClasses() throws Exception {
        URL archive = JarLauncher.class.getProtectionDomain().getCodeSource().getLocation();
        try (ZipFile jar = new ZipFile(new File(archive.toURI()))) {
            List<String> classes = jar.stream().map(ZipEntry::getName).filter(name -> name.endsWith(".class")).toList();
            assertTrue(classes.contains("net/hasor/boot/loader/internal/cobble/logging/LoggerFactory.class"));
            for (String name : classes) {
                assertTrue("Unexpected class: " + name, name.startsWith("net/hasor/boot/loader/"));
                if (name.startsWith("net/hasor/boot/loader/internal/")) {
                    assertTrue("Unused dependency class: " + name, name.startsWith("net/hasor/boot/loader/internal/cobble/logging/"));
                }
            }
        }
    }

    @Test
    public void applicationCobbleVersionAndLoggerSelectionAreIndependent() throws Exception {
        String factoryName = "net.hasor.cobble.logging.LoggerFactory";
        String internalFactoryName = "net.hasor.boot.loader.internal.cobble.logging.LoggerFactory";
        URL archive = JarLauncher.class.getProtectionDomain().getCodeSource().getLocation();
        Class<?> applicationCobble = Class.forName(factoryName, false, this.getClass().getClassLoader());
        URL cobbleArchive = applicationCobble.getProtectionDomain().getCodeSource().getLocation();
        assertTrue(cobbleArchive.getPath().endsWith("cobble-lang-5.0.2.jar"));

        try (URLClassLoader bootstrap = new URLClassLoader(new URL[] { archive }, ClassLoader.getPlatformClassLoader())) {
            Class<?> resourceType = bootstrap.loadClass(ResourceLoader.class.getName());
            Class<?> jarResourceType = bootstrap.loadClass(JarResourceLoader.class.getName());
            Object resources = jarResourceType.getConstructor(File.class).newInstance(new File(cobbleArchive.toURI()));
            Class<?> loaderType = bootstrap.loadClass(BootClassLoader.class.getName());
            Object instance = loaderType.getConstructor(ClassLoader.class, resourceType).newInstance(bootstrap, resources);
            ClassLoader application = (ClassLoader) instance;
            try (Closeable ignored = (Closeable) application) {
                Class<?> applicationFactory = application.loadClass(factoryName);
                Class<?> internalFactory = bootstrap.loadClass(internalFactoryName);
                assertSame(application, applicationFactory.getClassLoader());
                assertSame(bootstrap, internalFactory.getClassLoader());
                assertSame(internalFactory, application.loadClass(internalFactoryName));

                applicationFactory.getMethod("useNoLogger").invoke(null);
                Object applicationLogger = applicationFactory.getMethod("getLogger", String.class).invoke(null, "application");
                Object internalLogger = internalFactory.getMethod("getLogger", String.class).invoke(null, "bootstrap");
                assertEquals("net.hasor.cobble.logging.nologging.NoLoggingImpl", applicationLogger.getClass().getName());
                assertEquals("net.hasor.boot.loader.internal.cobble.logging.jdk14.Jdk14LoggingImpl", internalLogger.getClass().getName());
            }
        }
    }
}
