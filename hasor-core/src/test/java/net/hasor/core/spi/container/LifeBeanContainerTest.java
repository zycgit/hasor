/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
//package net.hasor.core.container;
//import net.hasor.core.*;
//import net.hasor.core.info.DefaultBindInfoProviderAdapter;
//import net.hasor.test.core.basic.destroy.PrototypePublicCallDestroyBean;
//import net.hasor.test.core.basic.destroy.SingletonPublicCallDestroyBean;
//import net.hasor.test.core.basic.init.PrototypePublicCallInitBean;
//import net.hasor.test.core.basic.init.StaticPublicCallInitBean;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import javax.inject.Singleton;
//import java.util.ArrayList;
//
//import static org.mockito.ArgumentMatchers.any;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ HasorUtils.class })
//public class LifeBeanContainerTest {
//    private BeanContainer beanContainer = null;
//    private AppContext    appContext    = null;
//
//    @Before
//    public void beforeTest() {
//        Environment env = Hasor.create().buildEnvironment();
//        this.beanContainer = new BeanContainer(env);
//        this.appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        this.beanContainer.init();
//    }
//
//    @Test
//    public void lifeTest1() {
//        Environment mockEnv = Hasor.create().buildEnvironment();
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        PrototypePublicCallInitBean bean = container.providerOnlyType(PrototypePublicCallInitBean.class, appContext, null).get();
//        assert bean.isInit();
//    }
//
//    @Test
//    public void lifeTest2() {
//        PowerMockito.mockStatic(HasorUtils.class);
//        ArrayList<Object> ref = new ArrayList<>();
//        PowerMockito.when(HasorUtils.pushShutdownListener(any(), (EventListener) any())).then(invocationOnMock -> {
//            ref.add(invocationOnMock.getArguments()[1]);
//            return null;
//        });
//        //
//        Environment mockEnv = Hasor.create().buildEnvironment();
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        PrototypePublicCallDestroyBean bean1 = container.providerOnlyType(PrototypePublicCallDestroyBean.class, appContext, null).get();
//        SingletonPublicCallDestroyBean bean2 = container.providerOnlyType(SingletonPublicCallDestroyBean.class, appContext, null).get();
//        //
//        assert ref.size() == 1;
//        assert !bean1.isDestroy();
//        assert !bean2.isDestroy();
//        //
//        for (Object obj : ref) {
//            try {
//                ((EventListener) obj).onEvent(null, appContext);
//            } catch (Throwable throwable) {
//            }
//        }
//        //
//        assert !bean1.isDestroy();
//        assert bean2.isDestroy();
//    }
//
//    @Test
//    public void lifeTest3() {
//        PowerMockito.mockStatic(HasorUtils.class);
//        ArrayList<Object> ref = new ArrayList<>();
//        PowerMockito.when(HasorUtils.pushStartListener(any(), (EventListener) any())).then(invocationOnMock -> {
//            ref.add(invocationOnMock.getArguments()[1]);
//            return null;
//        });
//        //
//        Environment mockEnv = Hasor.create().buildEnvironment();
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        //
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        DefaultBindInfoProviderAdapter<StaticPublicCallInitBean> adapter = container.getBindInfoContainer().createInfoAdapter(StaticPublicCallInitBean.class, null);
//        adapter.initMethod("init");
//        adapter.addScopeProvider(container.getScopeContainer().findScope(Singleton.class.getName()));
//        //
//        container.init();
//        //
//        assert ref.size() == 1;
//        assert !StaticPublicCallInitBean.isInit();
//        //
//        //
//        PowerMockito.when(appContext.getInstance((BindInfo) any())).then(invocationOnMock -> {
//            return container.providerOnlyBindInfo((BindInfo) invocationOnMock.getArguments()[0], appContext).get();
//        });
//        for (Object obj : ref) {
//            try {
//                ((EventListener) obj).onEvent(null, appContext);
//            } catch (Throwable throwable) {
//            }
//        }
//        //
//        assert StaticPublicCallInitBean.isInit();
//        StaticPublicCallInitBean.resetInit();
//    }
//}
