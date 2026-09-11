/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
//package net.hasor.core.container;
//import net.hasor.cobble.dynamic.DynamicClass;
//import net.hasor.cobble.dynamic.MethodInterceptor;
//import net.hasor.cobble.provider.InstanceProvider;
//import net.hasor.cobble.provider.Provider;
//import net.hasor.core.AppContext;
//import net.hasor.core.Environment;
//import net.hasor.core.Hasor;
//import net.hasor.core.info.AopBindInfoAdapter;
//import net.hasor.core.info.DefaultBindInfoProviderAdapter;
//import net.hasor.test.core.aop.custom.MyAopInterceptor;
//import net.hasor.test.core.aop.ignore.level.LevelFooFunction;
//import net.hasor.test.core.aop.ignore.level.l2.L2FooFunction;
//import net.hasor.test.core.aop.ignore.thread.ThreadFooFunction;
//import net.hasor.test.core.aop.ignore.types.*;
//import net.hasor.test.core.basic.pojo.PojoBean;
//import org.junit.Before;
//import org.junit.Test;
//import org.powermock.api.mockito.PowerMockito;
//
//import java.lang.reflect.Method;
//import java.util.function.Predicate;
//
//public class AopBeanContainerTest {
//    private AppContext appContext = null;
//
//    @Before
//    public void beforeTest() {
//        this.appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//    }
//
//    @Test
//    public void aopTest1() {
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        DefaultBindInfoProviderAdapter<AopBindInfoAdapter> adapter = container.getBindInfoContainer().createInfoAdapter(AopBindInfoAdapter.class, null);
//        Predicate<Class<?>> ma = aClass -> true;
//        Predicate<Method> mb = aMethod -> true;
//        MyAopInterceptor.resetInit();
//        MethodInterceptor interceptor = new MyAopInterceptor();
//        adapter.setCustomerProvider(Provider.of(new AopBindInfoAdapter(ma, mb, interceptor)));
//        //
//        PojoBean bean = container.providerOnlyType(PojoBean.class, appContext, null).get();
//        //
//        assert !MyAopInterceptor.isCalled();
//        bean.setUuid("abc");
//        assert MyAopInterceptor.isCalled();
//        MyAopInterceptor.resetInit();
//    }
//
//    @Test
//    public void aopTest2() {
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        DefaultBindInfoProviderAdapter<AopBindInfoAdapter> adapter = container.getBindInfoContainer().createInfoAdapter(AopBindInfoAdapter.class, null);
//        Predicate<Class<?>> ma = aClass -> false;
//        Predicate<Method> mb = aMethod -> true;
//        MyAopInterceptor.resetInit();
//        MethodInterceptor interceptor = new MyAopInterceptor();
//        adapter.setCustomerProvider(InstanceProvider.of(new AopBindInfoAdapter(ma, mb, interceptor)));
//        //
//        PojoBean bean = container.providerOnlyType(PojoBean.class, appContext, null).get();
//        //
//        assert !MyAopInterceptor.isCalled();
//        bean.setUuid("abc");
//        assert !MyAopInterceptor.isCalled();
//        MyAopInterceptor.resetInit();
//    }
//
//    @Test
//    public void aopTest3() {
//        Environment mockEnv = Hasor.create().buildEnvironment();
//        AppContext appContext = PowerMockito.mock(AppContext.class);
//        PowerMockito.when(appContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
//        BeanContainer container = new BeanContainer(mockEnv);
//        container.preInitialize();
//        //
//        DefaultBindInfoProviderAdapter<AopBindInfoAdapter> adapter = container.getBindInfoContainer().createInfoAdapter(AopBindInfoAdapter.class, null);
//        Predicate<Class<?>> ma = aClass -> true;
//        Predicate<Method> mb = aMethod -> true;
//        MethodInterceptor interceptor = new MyAopInterceptor();
//        adapter.setCustomerProvider(InstanceProvider.of(new AopBindInfoAdapter(ma, mb, interceptor)));
//        //
//        {
//            GrandFatherBean bean1 = container.providerOnlyType(GrandFatherBean.class, appContext, null).get();
//            JamesBean bean2 = container.providerOnlyType(JamesBean.class, appContext, null).get();
//            JamesSonBean bean3 = container.providerOnlyType(JamesSonBean.class, appContext, null).get();
//            WilliamBean bean4 = container.providerOnlyType(WilliamBean.class, appContext, null).get();
//            WilliamSonBean bean5 = container.providerOnlyType(WilliamSonBean.class, appContext, null).get();
//            assert bean1 instanceof DynamicClass;
//            assert !(bean2 instanceof DynamicClass);
//            assert bean3 instanceof DynamicClass;
//            assert !(bean4 instanceof DynamicClass);
//            assert !(bean5 instanceof DynamicClass);
//        }
//        //
//        {
//            L2FooFunction bean1 = container.providerOnlyType(L2FooFunction.class, appContext, null).get();
//            LevelFooFunction bean2 = container.providerOnlyType(LevelFooFunction.class, appContext, null).get();
//            ThreadFooFunction bean3 = container.providerOnlyType(ThreadFooFunction.class, appContext, null).get();
//            assert !(bean1 instanceof DynamicClass);
//            assert bean2 instanceof DynamicClass;
//            assert !(bean3 instanceof DynamicClass);
//        }
//        //
//        //
//    }
//}
