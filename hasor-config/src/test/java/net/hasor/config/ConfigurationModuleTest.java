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

import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.core.ConfigurationModule;
import net.hasor.core.AppContext;
import net.hasor.core.ApiBinder;
import net.hasor.core.CircularDependencyException;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.core.Module;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationModuleTest {
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
        return Hasor.create().addSettings(Settings.DefaultNameSpace, AutoConfigurationModule.AUTO_SCAN_ENABLED, false);
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
        private static final AtomicInteger destroyed = new AtomicInteger();
        private boolean ready;

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
