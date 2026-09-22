/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.core.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DependsOnTest {
    private static final List<String> EVENTS = new ArrayList<>();

    public static class First {
        @Init
        public void init() {
            EVENTS.add("first.init");
        }
    }

    @DependsOn("first")
    public static class Second {
        public Second() {
            EVENTS.add("second.new");
        }
    }

    @Test
    public void annotationResolvesLaterModuleAndCompletesInitializationBeforeConstruction() throws Throwable {
        EVENTS.clear();
        try (AppContext context = Hasor.create().build(
                binder -> binder.bindType(Second.class),
                binder -> binder.bindType(First.class).idWith("first"))) {
            context.getInstance(Second.class);
            assertEquals(List.of("first.init", "second.new"), EVENTS);
        }
    }

    @Test
    public void providerDependenciesRespectSingletonAndPrototype() throws Throwable {
        for (boolean singleton : new boolean[] { true, false }) {
            AtomicInteger dependencies = new AtomicInteger();
            AtomicInteger creations = new AtomicInteger();
            try (AppContext context = Hasor.create().build(binder -> {
                var binding = binder.bindType(String.class).idWith("result").toProvider(() -> {
                    assertTrue(dependencies.get() > 0);
                    creations.incrementAndGet();
                    return "ready";
                }).dependsOn("dependency");
                if (singleton) {
                    binding.asEagerSingleton();
                }
                binder.bindType(Integer.class).idWith("dependency").toProvider(dependencies::incrementAndGet);
            })) {
                assertEquals("ready", context.getInstance("result"));
                assertEquals("ready", context.getInstance("result"));
                assertEquals(singleton ? 1 : 2, creations.get());
                assertEquals(singleton ? 1 : 2, dependencies.get());
            }
        }
    }

    @Test
    public void namedTypedProviderWorksBeforeStartedEventAndRejectsAmbiguity() throws Throwable {
        try (AppContext context = Hasor.create().build(binder -> {
            var source = binder.getProvider("chosen", Integer.class);
            binder.bindType(String.class).toProvider(() -> source.get().toString()).dependsOn("chosen", Integer.class);
            binder.bindType(Integer.class).nameWith("chosen").toInstance(7);
            binder.bindType(Integer.class).nameWith("other").toInstance(8);
            binder.bindSpiListener(net.hasor.core.spi.ContextStartListener.class,
                    new net.hasor.core.spi.ContextStartListener() {
                        public void doStart(AppContext app) {
                            assertEquals("7", app.getInstance(String.class));
                        }
                        public void doStartCompleted(AppContext app) {
                        }
                    });
        })) {
            assertEquals("7", context.getInstance(String.class));
        }
        try (AppContext context = Hasor.create().build(binder -> {
            binder.bindType(String.class).toProvider(() -> "never").dependsOn(Integer.class);
            binder.bindType(Integer.class).nameWith("a").toInstance(1);
            binder.bindType(Integer.class).nameWith("b").toInstance(2);
        })) {
            try {
                context.getInstance(String.class);
                fail("Ambiguous dependencies must fail");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("found 2"));
            }
        }
    }

    @Test
    public void typeAndBindingReferenceDependenciesAccumulateWithoutDuplicateLookups() throws Throwable {
        AtomicInteger calls = new AtomicInteger();
        try (AppContext context = Hasor.create().build(binder -> {
            var dependency = binder.bindType(Integer.class).idWith("number").toProvider(calls::incrementAndGet).toInfo();
            binder.bindType(String.class).toProvider(() -> "ready").dependsOn(dependency).dependsOn("number");
            binder.bindType(Long.class).toProvider(() -> 1L).dependsOn(Integer.class);
        })) {
            assertEquals("ready", context.getInstance(String.class));
            assertEquals(1, calls.get());
            assertEquals(Long.valueOf(1), context.getInstance(Long.class));
            assertEquals(2, calls.get());
        }
    }

    @Test
    public void missingAndFailedDependenciesPreventFactoryInvocation() throws Throwable {
        for (boolean missing : new boolean[] { true, false }) {
            AtomicInteger creations = new AtomicInteger();
            try (AppContext context = Hasor.create().build(binder -> {
                binder.bindType(String.class).toProvider(() -> {
                    creations.incrementAndGet();
                    return "never";
                }).dependsOn("dependency");
                if (!missing) {
                    binder.bindType(Integer.class).idWith("dependency").toProvider(() -> {
                        throw new IllegalStateException("dependency failed");
                    });
                }
            })) {
                try {
                    context.getInstance(String.class);
                    fail("Dependency failure must propagate");
                } catch (IllegalStateException expected) {
                    assertTrue(expected.getMessage().contains("dependency"));
                }
                assertEquals(0, creations.get());
            }
        }
    }

    public static class Injected {
        @Inject
        private Other other;
    }

    public static class Other {
    }

    @Test
    public void mixedInjectionCycleAndSelfDependencyHaveCreationDiagnostics() throws Throwable {
        try (AppContext context = Hasor.create().build(binder -> {
            binder.bindType(Injected.class).idWith("injected");
            binder.bindType(Other.class).dependsOn("injected");
            binder.bindType(String.class).idWith("self").toProvider(() -> "never").dependsOn("self");
        })) {
            for (String id : List.of("injected", "self")) {
                try {
                    context.getInstance(id);
                    fail("Cycle must fail");
                } catch (CircularDependencyException expected) {
                    assertTrue(expected.getMessage().contains(id));
                }
            }
        }
    }
}
