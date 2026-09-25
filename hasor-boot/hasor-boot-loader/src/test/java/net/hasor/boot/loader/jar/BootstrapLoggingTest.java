/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.Test;
import static org.junit.Assert.*;

public class BootstrapLoggingTest {
    @Test
    public void embeddedLoggerWorksWithoutApplicationDependencies() throws Exception {
        URL classes = Handler.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[] { classes }, ClassLoader.getPlatformClassLoader())) {
            try {
                loader.loadClass("net.hasor.cobble.logging.LoggerFactory");
                fail("The loader must not expose Cobble's original package");
            } catch (ClassNotFoundException expected) {
                // The application's Cobble is not part of the bootstrap classpath.
            }
            Class<?> factory = loader.loadClass("net.hasor.boot.loader.internal.cobble.logging.LoggerFactory");
            Object embeddedLogger = factory.getMethod("getLogger", String.class).invoke(null, Handler.class.getName());
            assertEquals("net.hasor.boot.loader.internal.cobble.logging.jdk14.Jdk14LoggingImpl", embeddedLogger.getClass().getName());
            Class<?> handlerType = loader.loadClass(Handler.class.getName());
            Object handler = handlerType.getConstructor().newInstance();
            Method log = handlerType.getDeclaredMethod("log", boolean.class, String.class, Exception.class);
            log.setAccessible(true);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Logger logger = Logger.getLogger(Handler.class.getName());
            Level originalLevel = logger.getLevel();
            boolean originalParentHandlers = logger.getUseParentHandlers();
            StreamHandler capture = new StreamHandler(output, new SimpleFormatter());
            capture.setEncoding(StandardCharsets.UTF_8.name());
            capture.setLevel(Level.ALL);
            logger.addHandler(capture);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            try {
                log.invoke(handler, true, "bootstrap warning", new Exception("test"));
                log.invoke(handler, false, "bootstrap trace", new Exception("test"));
                capture.flush();
            } finally {
                logger.removeHandler(capture);
                logger.setLevel(originalLevel);
                logger.setUseParentHandlers(originalParentHandlers);
                capture.close();
            }
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("bootstrap warning"));
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("bootstrap trace"));
        }
    }
}
