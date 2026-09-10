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
package net.hasor.config;
import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.*;
import net.hasor.core.Module;

public class ConfigurationModuleTest {
    @Test
    public void publicDefaultConstructorAndProtectedSubclassConstructor() throws Exception {
        assertEquals(1, ConfigurationModule.class.getConstructors().length);
        assertNotNull(ConfigurationModule.class.getConstructor().newInstance());
        assertTrue(java.lang.reflect.Modifier.isProtected(ConfigurationModule.class.getDeclaredConstructor(Class[].class).getModifiers()));
        try (AppContext context = manualHasor().build(new DerivedConfigurationModule())) {
            assertEquals("hello Hasor", context.getInstance(MessageService.class).message());
        }
    }

    private static class DerivedConfigurationModule extends ConfigurationModule {
        DerivedConfigurationModule() {
            super(AppConfig.class);
        }
    }

    @Test
    public void binderScanUsesExplicitScope() throws Exception {
        try (AppContext context = manualHasor().build(binder -> binder.installModule(ConfigurationModule.of(binder.findClass(Configuration.class, "net.hasor.config.autoscan").toArray(Class<?>[]::new))))) {
            assertEquals("module-loaded", context.getInstance(net.hasor.config.autoscan.AutoScanService.class).getValue());
        }
    }

    @Test
    public void explicitModesDoNotDiscoverExtensionModules() throws Throwable {
        ApiBinder binder = org.mockito.Mockito.mock(ApiBinder.class);
        ConfigurationModule.of().loadModule(binder);
        org.mockito.Mockito.verifyNoMoreInteractions(binder);
    }

    @Test
    public void autoFactoryUsesCoreScope() throws Exception {
        try (AppContext context = manualHasor().addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.autoscan").build(ConfigurationModule.auto())) {
            assertEquals("module-loaded", context.getInstance(net.hasor.config.autoscan.AutoScanService.class).getValue());
        }
    }

    @Test
    public void configurationShouldCreateBeansAndInjectMethodParameters() {
        AppContext context = manualHasor().build(ConfigurationModule.of(AppConfig.class));

        MessageService service = context.getInstance(MessageService.class);
        assertEquals("hello Hasor", service.message());
        assertSame(service.repository, context.getInstance(MessageRepository.class));
        assertSame(context.getInstance(MessageRepository.class), context.getInstance("repository"));
        assertSame(context.getInstance(AppConfig.class), context.getInstance(AppConfig.class));
    }

    @Test
    public void beanShouldSupportNameAndPrototypeScope() {
        AppContext context = manualHasor().build(ConfigurationModule.of(AppConfig.class));

        assertEquals("named", context.getInstance("namedText"));
        assertNotSame(context.getInstance(Sequence.class), context.getInstance(Sequence.class));
    }

    @Test
    public void configurationClassShouldUseHasorInjection() {
        AppContext context = manualHasor().build(apiBinder -> {
            apiBinder.bindType(Prefix.class).toInstance(new Prefix("injected "));
            apiBinder.installModule(ConfigurationModule.of(InjectedConfig.class));
        });

        assertEquals("injected config", context.getInstance(ConfigValue.class).value);
    }

    @Test
    public void beanShouldRunLifecycleMethods() throws Exception {
        LifecycleBean.initialized.set(0);
        LifecycleBean.destroyed.set(0);
        AppContext context = manualHasor().build(ConfigurationModule.of(LifecycleConfig.class));

        LifecycleBean bean = context.getInstance(LifecycleBean.class);
        assertTrue(bean.ready);
        assertEquals(1, LifecycleBean.initialized.get());
        context.shutdown();
        assertEquals(1, LifecycleBean.destroyed.get());
    }

    @Test
    public void beanMethodCycleShouldReportVisualFactoryMethodPath() {
        AppContext context = manualHasor().build(ConfigurationModule.of(CircularConfig.class));

        try {
            context.getInstance(CircularA.class);
            fail("CircularDependencyException expected.");
        } catch (CircularDependencyException e) {
            assertTrue(e.getMessage().contains("CircularConfig.circularA(CircularB)"));
            assertTrue(e.getMessage().contains("CircularConfig.circularB(CircularA)"));
            assertTrue(e.getMessage().contains("+--"));
            assertTrue(e.getMessage().contains("\\--"));
        }
    }

    @Test
    public void moduleConfigurationShouldRegisterBeansBeforeInstallingModule() {
        BeanBeforeModuleConfig.beanRegisteredWhenModuleLoads = false;
        AppContext context = manualHasor().build(ConfigurationModule.of(BeanBeforeModuleConfig.class));

        assertTrue(BeanBeforeModuleConfig.beanRegisteredWhenModuleLoads);
        assertNotNull(context.getInstance(MessageRepository.class));
    }

    private Hasor manualHasor() {
        return Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.no_autoscan");
    }

    @Configuration
    public static class AppConfig {
        @Bean
        public MessageRepository repository() {
            return new MessageRepository("hello ");
        }

        @Bean
        public MessageService messageService(MessageRepository repository) {
            return new MessageService(repository);
        }

        @Bean("namedText")
        public String namedText() {
            return "named";
        }

        @Bean(singleton = false)
        public Sequence sequence() {
            return new Sequence();
        }
    }

    @Configuration
    public static class InjectedConfig {
        @Inject
        private Prefix prefix;

        @Bean
        public ConfigValue configValue() {
            return new ConfigValue(this.prefix.value + "config");
        }
    }

    @Configuration
    public static class LifecycleConfig {
        @Bean(initMethod = "init", destroyMethod = "destroy")
        public LifecycleBean lifecycleBean() {
            return new LifecycleBean();
        }
    }

    @Configuration
    public static class CircularConfig {
        @Bean
        public CircularA circularA(CircularB circularB) {
            return new CircularA();
        }

        @Bean
        public CircularB circularB(CircularA circularA) {
            return new CircularB();
        }
    }

    @Configuration
    public static class BeanBeforeModuleConfig implements Module {
        private static boolean beanRegisteredWhenModuleLoads;

        @Bean
        public MessageRepository repository() {
            return new MessageRepository("configured ");
        }

        @Override
        public void loadModule(ApiBinder apiBinder) {
            beanRegisteredWhenModuleLoads = apiBinder.findBindingRegister("", MessageRepository.class) != null;
        }
    }

    public static class MessageRepository {
        private final String prefix;

        public MessageRepository(String prefix) {
            this.prefix = prefix;
        }
    }

    public static class MessageService {
        private final MessageRepository repository;

        public MessageService(MessageRepository repository) {
            this.repository = repository;
        }

        public String message() {
            return this.repository.prefix + "Hasor";
        }
    }

    public static class Sequence {
    }

    public static class Prefix {
        private final String value;

        public Prefix(String value) {
            this.value = value;
        }
    }

    public static class ConfigValue {
        private final String value;

        public ConfigValue(String value) {
            this.value = value;
        }
    }

    public static class LifecycleBean {
        private static final AtomicInteger initialized = new AtomicInteger();
        private static final AtomicInteger destroyed   = new AtomicInteger();
        private boolean                    ready;

        public void init() {
            this.ready = true;
            initialized.incrementAndGet();
        }

        public void destroy() {
            destroyed.incrementAndGet();
        }
    }

    public static class CircularA {
    }

    public static class CircularB {
    }
}
