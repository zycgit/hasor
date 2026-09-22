/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.ArrayList;
import java.util.List;
import net.hasor.core.AppContext;
import net.hasor.core.CircularDependencyException;
import net.hasor.core.DependsOn;
import net.hasor.core.Hasor;
import org.junit.Test;
import static org.junit.Assert.*;

public class DependsOnConfigurationTest {
    private static final List<String> EVENTS = new ArrayList<>();

    @Configuration
    public static class ConsumerConfig {
        @Bean("consumer")
        @DependsOn("storage")
        public String consumer() {
            EVENTS.add("consumer");
            return "ready";
        }
    }

    @Configuration
    public static class StorageConfig {
        @Bean(value = "storage", initMethod = "init")
        public Storage storage() {
            EVENTS.add("storage.new");
            return new Storage();
        }
    }

    public static class Storage {
        public void init() {
            EVENTS.add("storage.init");
        }
    }

    @Test
    public void methodAnnotationUsesCoreDependenciesAcrossConfigurations() throws Throwable {
        EVENTS.clear();
        try (AppContext context = Hasor.create().build(ConfigurationModule.of(ConsumerConfig.class, StorageConfig.class))) {
            assertEquals("ready", context.getInstance("consumer"));
            assertEquals("ready", context.getInstance("consumer"));
            assertEquals(List.of("storage.new", "storage.init", "consumer"), EVENTS);
        }
    }

    @Configuration
    public static class CycleConfig {
        @Bean("a")
        @DependsOn("b")
        public String a() {
            return "a";
        }

        @Bean("b")
        @DependsOn("a")
        public Integer b() {
            return 1;
        }
    }

    @Test
    public void factoryCycleUsesCoreDiagnostics() throws Throwable {
        try (AppContext context = Hasor.create().build(ConfigurationModule.of(CycleConfig.class))) {
            try {
                context.getInstance("a");
                fail("Cycle must fail");
            } catch (CircularDependencyException expected) {
                assertTrue(expected.getMessage().contains("CycleConfig.a"));
                assertTrue(expected.getMessage().contains("CycleConfig.b"));
            }
        }
    }
}
