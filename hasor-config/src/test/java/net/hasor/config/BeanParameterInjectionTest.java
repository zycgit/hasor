/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.parameter.ParameterConfiguration;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeanParameterInjectionTest {
    @Test
    public void resolvesDefaultNamedAndIdParametersAfterBindingsAreRegistered() throws Exception {
        try (AppContext context = this.hasor().build(binder -> {
            this.bindDependencies(binder, () -> "first");
            binder.bindType(String.class).nameWith("second").toInstance("second");
            binder.bindType(Integer.class).nameWith("first").toInstance(100);
        })) {
            assertEquals(List.of("main", "main", "main", "first", "second", "by-id"), context.getInstance(List.class));
        }
    }

    private Hasor hasor() {
        return Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.no_autoscan");
    }

    private void bindDependencies(ApiBinder binder, Supplier<String> first) throws Throwable {
        binder.installModule(ConfigurationModule.of(ParameterConfiguration.class));
        binder.bindType(String.class).toInstance("main");
        binder.bindType(String.class).nameWith("first").toProvider(first).asEagerPrototype();
        binder.bindType(String.class).idWith("reference").nameWith("different-name").toInstance("by-id");
        binder.bindType(String.class).nameWith("reference").toInstance("by-name");
    }

    @Test
    public void resolvesPrototypeParametersForEachFactoryInvocation() throws Exception {
        AtomicInteger created = new AtomicInteger();
        try (AppContext context = this.hasor().build(binder -> {
            this.bindDependencies(binder, () -> "first-" + created.incrementAndGet());
            binder.bindType(String.class).nameWith("second").toInstance("second");
        })) {
            List<?> first = context.getInstance(List.class);
            int count = created.get();
            List<?> second = context.getInstance(List.class);
            assertNotSame(first, second);
            assertEquals(count + 1, created.get());
            assertEquals("first-" + count, first.get(3));
            assertEquals("first-" + (count + 1), second.get(3));
        }
    }

    @Test
    public void missingNamedParameterDoesNotUseTheDefaultBean() throws Exception {
        try (AppContext context = this.hasor().build(binder -> this.bindDependencies(binder, () -> "first"))) {
            try {
                context.getInstance(List.class);
                fail("A missing named dependency must fail.");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("named 'second'"));
                assertTrue(expected.getMessage(), expected.getMessage().contains("found 0"));
            }
        }
    }
}
