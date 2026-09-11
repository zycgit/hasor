/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.config.Bean;
import net.hasor.config.Configuration;
import net.hasor.core.Module;
import org.junit.Test;
import static org.junit.Assert.*;

public class BootTest {
    @Configuration
    public static class Source {
        @Bean
        public String message() {
            return "boot";
        }
    }

    @Test
    public void runsWithoutWebAndProcessesExplicitConfiguration() throws Exception {
        try (BootApplication application = new Boot().sources(Source.class).property("hasor.loadPackages", "example.empty").start()) {
            assertEquals("boot", application.getAppContext().getInstance(String.class));
            assertTrue(application.getAppContext().isStart());
            try {
                Class.forName("javax.servlet.ServletContext");
                fail("Runtime must not bring Servlet onto its classpath.");
            } catch (ClassNotFoundException expected) {
                // Web 是可选扩展。
            }
        }
    }

    @Test
    public void closesResourcesInReverseOrderEvenWhenOneFails() throws Exception {
        BootApplication application = new Boot().property("hasor.loadPackages", "example.empty").start();
        StringBuilder order = new StringBuilder();
        application.onClose(() -> order.append("first"));
        application.onClose(() -> {
            order.append("second");
            throw new IllegalStateException("failure");
        });
        try {
            application.close();
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("failure", expected.getMessage());
        }
        application.close();
        assertEquals("secondfirst", order.toString());
        assertFalse(application.getAppContext().isStart());
    }

    @Test
    public void extensionsComposeByOrder() throws Exception {
        FirstTestExtension.events.clear();
        try (BootApplication application = new Boot().property("hasor.loadPackages", "example.empty").start()) {
            assertEquals(java.util.List.of("first-start", "second-start"), FirstTestExtension.events);
        }
        assertEquals(java.util.List.of("first-start", "second-start", "first-stop", "second-stop"), FirstTestExtension.events);
    }

    @Test
    public void moduleLifecycleIsInvokedOnce() throws Exception {
        Lifecycle.starts.set(0);
        Lifecycle.stops.set(0);
        BootApplication application = new Boot().sources(Lifecycle.class).arguments("hello").property("hasor.loadPackages", "example.empty").start();
        assertEquals(1, Lifecycle.starts.get());
        assertArrayEquals(new String[] { "hello" }, application.getAppContext().getInstance(net.hasor.core.info.Arguments.class).args());
        application.close();
        application.close();
        assertEquals(1, Lifecycle.stops.get());
    }

    public static class Lifecycle implements Module {
        static final AtomicInteger starts = new AtomicInteger();
        static final AtomicInteger stops  = new AtomicInteger();

        public void loadModule(net.hasor.core.ApiBinder binder) {
        }

        public void onStart(net.hasor.core.AppContext context) {
            starts.incrementAndGet();
        }

        public void onStop(net.hasor.core.AppContext context) {
            stops.incrementAndGet();
        }
    }
}
