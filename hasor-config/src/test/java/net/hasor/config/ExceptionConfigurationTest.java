/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletContext;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.web.Exception;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.web.ExceptionHandler;
import net.hasor.web.Invoker;
import net.hasor.web.WebApiBinder;
import net.hasor.web.binder.ExceptionDef;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExceptionConfigurationTest {
    private Hasor webHasor() {
        ServletContext servlet = mock(ServletContext.class);
        when(servlet.getClassLoader()).thenReturn(this.getClass().getClassLoader());
        when(servlet.getContextPath()).thenReturn("/");
        when(servlet.getEffectiveMajorVersion()).thenReturn(3);
        when(servlet.getVirtualServerName()).thenReturn("test");
        return Hasor.create(servlet).addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.no_autoscan");
    }

    @Test
    public void factoryUsesInjectionAndSharesOneBeanAcrossExceptionTypes() throws Throwable {
        FactoryConfig.calls.set(0);
        try (AppContext app = this.webHasor().build(binder -> {
            binder.bindType(Prefix.class).toInstance(new Prefix("injected"));
            binder.installModule(ConfigurationModule.of(FactoryConfig.class));
            assertEquals(0, FactoryConfig.calls.get());
        })) {
            ExceptionDef<?>[] handlers = app.getInstance(ExceptionDef[].class);
            assertEquals(2, handlers.length);
            Invoker invoker = mock(Invoker.class);
            for (int i = 0; i < 2; i++) {
                for (ExceptionDef<?> handler : handlers) {
                    Throwable error = handler.getExceptionType().getConstructor(String.class).newInstance("failure");
                    assertEquals("injected:dependency:failure", handler.handleException(invoker, error));
                }
            }
            assertEquals(1, FactoryConfig.calls.get());
            assertSame(app.getInstance("failures"), app.getInstance("failures"));
        }
    }

    @Test
    public void beanAnnotationAddsNameAndLifecycleWithoutCreatingADuplicate() throws Throwable {
        ManagedHandler.created.set(0);
        ManagedHandler.destroyed.set(0);
        try (AppContext app = this.webHasor().build(ConfigurationModule.of(LifecycleConfig.class))) {
            ManagedHandler bean = (ManagedHandler) app.getInstance("hostErrors");
            assertTrue(bean.initialized);
            ExceptionDef<?> handler = app.getInstance(ExceptionDef[].class)[0];
            assertEquals("ready", handler.handleException(mock(Invoker.class), new IOException()));
            assertEquals(1, ManagedHandler.created.get());
        }
        assertEquals(1, ManagedHandler.destroyed.get());
    }

    @Test
    public void automaticScanFindsExceptionFactories() throws Throwable {
        try (AppContext app = this.webHasor()
                .addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.exceptionconfig")
                .build()) {
            ExceptionDef<?>[] handlers = app.getInstance(ExceptionDef[].class);
            assertEquals(1, handlers.length);
            assertEquals("scanned:failure", handlers[0].handleException(mock(Invoker.class), new IOException("failure")));
        }
    }

    @Test
    public void invalidFactoriesFailAtStartup() {
        for (Class<?> config : Arrays.asList(WrongReturnConfig.class, EmptyTypesConfig.class, StaticFactoryConfig.class)) {
            try (AppContext ignored = this.webHasor().build(ConfigurationModule.of(config))) {
                fail("Invalid exception factories must fail during startup");
            } catch (Throwable failure) {
                assertTrue(this.hasMessage(failure, "@Exception") || this.hasMessage(failure, "must not be static"));
            }
        }
    }

    private boolean hasMessage(Throwable failure, String text) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(text)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void manualAndAnnotatedRegistrationsUseTheSameRegistry() {
        try (AppContext ignored = this.webHasor().build(binder -> {
            binder.tryCast(WebApiBinder.class).addExceptionHandler(IOException.class, (invoker, error) -> "manual");
            binder.installModule(ConfigurationModule.of(LifecycleConfig.class));
        })) {
            fail("The same exception type must not be registered twice");
        } catch (Throwable failure) {
            assertTrue(this.hasMessage(failure, "Exception handler already registered"));
        }
    }

    @Configuration
    public static class FactoryConfig {
        static final AtomicInteger calls = new AtomicInteger();
        @Inject
        private Prefix prefix;

        @Bean
        public Message dependency() {
            return new Message("dependency");
        }

        @Exception({IOException.class, IllegalStateException.class})
        public ExceptionHandler<Throwable> failures(Message dependency) {
            calls.incrementAndGet();
            return (invoker, error) -> this.prefix.value + ":" + dependency.value + ":" + error.getMessage();
        }
    }

    public record Prefix(String value) {
    }

    public record Message(String value) {
    }

    @Configuration
    public static class LifecycleConfig {
        @Bean(value = "hostErrors", initMethod = "init", destroyMethod = "destroy")
        @Exception(IOException.class)
        public ManagedHandler failures() {
            return new ManagedHandler();
        }
    }

    public static class ManagedHandler implements ExceptionHandler<IOException> {
        static final AtomicInteger created = new AtomicInteger();
        static final AtomicInteger destroyed = new AtomicInteger();
        private boolean initialized;

        public ManagedHandler() {
            created.incrementAndGet();
        }

        public void init() {
            this.initialized = true;
        }

        public void destroy() {
            destroyed.incrementAndGet();
        }

        @Override
        public Object handleException(Invoker invoker, IOException error) {
            assertTrue(this.initialized);
            return "ready";
        }
    }

    @Configuration
    public static class WrongReturnConfig {
        @Exception(IOException.class)
        public String error() {
            return "wrong";
        }
    }

    @Configuration
    public static class EmptyTypesConfig {
        @Exception({})
        public ExceptionHandler<Throwable> error() {
            return (invoker, error) -> "empty";
        }
    }

    @Configuration
    public static class StaticFactoryConfig {
        @Exception(IOException.class)
        public static ExceptionHandler<Throwable> error() {
            return (invoker, error) -> "static";
        }
    }
}
