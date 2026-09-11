/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.container.BeanContainer;
import net.hasor.core.container.BindInfoContainer;
import net.hasor.core.container.ScopeContainer;
import net.hasor.core.container.SpiCallerContainer;
import net.hasor.core.info.DefaultBindInfoProviderAdapter;
import net.hasor.core.info.GenerateBeanID;
import org.junit.Before;

public class AbstractBinderDataTest {
    protected Predicate<Class<?>>                             ignoreMatcher;
    protected AtomicReference<DefaultBindInfoProviderAdapter> reference;
    protected ApiBinderWrap                                   binder;

    @Before
    public void beforeTest() throws IOException {
        this.reference = new AtomicReference<>();
        SpiCallerContainer spiContainer = new SpiCallerContainer();
        BindInfoContainer bindInfoContainer = new BindInfoContainer(spiContainer) {
            private final GenerateBeanID generateBeanID = new GenerateBeanID();

            @Override
            public <T> DefaultBindInfoProviderAdapter<T> createInfoAdapter(Class<T> bindType, ApiBinder apiBinder) {
                DefaultBindInfoProviderAdapter<T> adapter = new DefaultBindInfoProviderAdapter<>(bindType, this.generateBeanID);
                Predicate<Class<?>> defaultMatcher = (ignoreMatcher == null) ? (aClass -> false) : ignoreMatcher;
                if (!defaultMatcher.test(bindType)) {
                    reference.set(adapter);
                }
                return adapter;
            }
        };
        ScopeContainer scopeContainer = new ScopeContainer(spiContainer);
        scopeContainer.init();
        BindInfoBuilderFactory factory = new BindInfoBuilderFactory() {
            @Override
            public Settings getSettings() {
                return net.hasor.core.Hasor.create().buildSettings();
            }

            @Override
            public SpiCallerContainer getSpiContainer() {
                return spiContainer;
            }

            @Override
            public BindInfoContainer getBindInfoContainer() {
                return bindInfoContainer;
            }

            @Override
            public ScopeContainer getScopeContainer() {
                return scopeContainer;
            }
        };
        this.binder = new ApiBinderWrap(newAbstractBinder(factory));
    }

    protected BasicBinder newAbstractBinder(BindInfoBuilderFactory factory) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BeanContainer context = new BeanContainer(net.hasor.core.Hasor.create().buildSettings(), classLoader, new ClassPathResourceLoader(classLoader), null);
        AtomicReference<ApiBinder> refApiBinder = new AtomicReference<>();
        BasicBinder basicBinder = new BasicBinder(context) {
            @Override
            protected ApiBinder self() {
                return refApiBinder.get();
            }

            @Override
            protected BindInfoBuilderFactory containerFactory() {
                return factory;
            }
        };
        refApiBinder.set(this.binder);
        return basicBinder;
    }
}
