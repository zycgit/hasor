/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class BootstrapLoggingTest {
    @Test
    public void warningFallsBackWithoutCobbleOnBootstrapClasspath() throws Exception {
        URL classes = Handler.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[] { classes }, ClassLoader.getPlatformClassLoader())) {
            try {
                loader.loadClass("net.hasor.cobble.logging.LoggerFactory");
                fail("Cobble must be absent in this bootstrap test");
            } catch (ClassNotFoundException expected) {
            }
            Class<?> handlerType = loader.loadClass(Handler.class.getName());
            Object handler = handlerType.getConstructor().newInstance();
            Method log = handlerType.getDeclaredMethod("log", boolean.class, String.class, Exception.class);
            log.setAccessible(true);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream original = System.err;
            try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                System.setErr(capture);
                log.invoke(handler, true, "bootstrap warning", new Exception("test"));
                log.invoke(handler, false, "silent trace", new Exception("test"));
            } finally {
                System.setErr(original);
            }
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("WARNING: bootstrap warning"));
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("silent trace"));
        }
    }
}
