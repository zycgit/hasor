/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.provider.Provider;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.Hasor;
import net.hasor.core.info.AopBindInfoAdapter;
import net.hasor.core.info.DefaultBindInfoProviderAdapter;
import net.hasor.test.core.MockBindInfo;
import net.hasor.test.core.basic.pojo.PojoBeanTestBeanC;
import net.hasor.test.core.basic.pojo.PojoBeanTestBeanP;
import net.hasor.test.core.binder.TestBinder;
import org.junit.Test;

public class BinderDataTest extends AbstractBinderDataTest {
    @Test
    public void metaDataTest1() {
        AppContext appContext = Hasor.create().build();

        binder.bindType(BinderDataTest.class);
        assert reference.get().getBindType() == BinderDataTest.class;
        //
        binder.bindType(BinderDataTest.class).metaData("test", "value");
        assert "value".equals(reference.get().getMetaData("test"));
        reference.get().removeMetaData("test");
    }

    @Test
    public void metaDataTest2() {
        binder.bindType(PojoBeanTestBeanP.class).metaData("metaKey", "metaValue");
        assert "metaValue".equals(reference.get().getMetaData("metaKey"));
    }

    @Test
    public void metaDataTest3() {
        Method target = BinderDataTest.class.getDeclaredMethods()[0];
        binder.bindType(PojoBeanTestBeanP.class).metaData("metaKey", target);
        assert target == reference.get().getMetaData("metaKey");
    }

    @Test
    public void bindTest1() {
        List<Object> list = new ArrayList<>();
        binder.bindType(List.class, list);
        assert reference.get().getBindType() == List.class;
        assert reference.get().getCustomerProvider().get() == list;
    }

    @Test
    public void bindTest2() {
        binder.bindType(List.class, LinkedList.class);
        assert reference.get().getBindType() == List.class;
        assert reference.get().getSourceType() == LinkedList.class;
    }

    @Test
    public void bindTest3() {
        Date self = new Date();
        Supplier<Date> selfProvider = Provider.of(self);
        binder.bindType(Date.class, selfProvider);
        assert reference.get().getBindType() == Date.class;
        assert reference.get().getSourceType() == null;
        assert reference.get().getCustomerProvider() == selfProvider;
        assert reference.get().getCustomerProvider().get() == self;
    }

    @Test
    public void bindTest4() {
        binder.bindType("abc", ArrayList.class);
        assert reference.get().getBindType() == ArrayList.class;
        assert !reference.get().getBindName().equals(reference.get().getBindID());
        assert reference.get().getBindName().equals("abc");
    }

    @Test
    public void bindTest5() {
        List<Object> list = new ArrayList<>();
        binder.bindType("myList", List.class, list);
        assert reference.get().getBindType() == List.class;
        assert reference.get().getCustomerProvider().get() == list;
        assert reference.get().getBindName().equalsIgnoreCase("myList");
    }

    @Test
    public void bindTest6() {
        binder.bindType("myLinkedList", List.class, LinkedList.class);
        assert reference.get().getBindType() == List.class;
        assert reference.get().getSourceType() == LinkedList.class;
        assert reference.get().getBindName().equalsIgnoreCase("myLinkedList");
    }

    @Test
    public void bindTest7() {
        Date self = new Date();
        Supplier<Date> selfProvider = Provider.of(self);
        binder.bindType("myDate", Date.class, selfProvider);
        assert reference.get().getBindType() == Date.class;
        assert reference.get().getSourceType() == null;
        assert reference.get().getCustomerProvider() == selfProvider;
        assert reference.get().getCustomerProvider().get() == self;
        assert reference.get().getBindName().equalsIgnoreCase("myDate");
    }

    @Test
    public void bindTest8() {
        binder.bindType(BinderDataTest.class).idWith("12345");
        assert reference.get().getBindType() == BinderDataTest.class;
        assert "12345".equals(reference.get().getBindID());
        assert reference.get().getBindName() == null;
        //
        binder.bindType(BinderDataTest.class).bothWith("12345");
        assert "12345".equals(reference.get().getBindID());
        assert "12345".equals(reference.get().getBindName());
    }

    @Test
    public void bindTest9() {
        binder.getBindInfo("tttt");
        binder.getBindInfo(TestBinder.class);
        binder.findBindingRegister("", TestBinder.class);
        binder.findBindingRegister(TestBinder.class);
    }

    @Test
    public void lifeTest1() {
        binder.bindType(PojoBeanTestBeanP.class).initMethod("doInit");
        DefaultBindInfoProviderAdapter<?> adapter = reference.get();
        Class<?> targetType = adapter.getSourceType() != null ? adapter.getSourceType() : adapter.getBindType();
        assert adapter.getInitMethod(targetType) != null;
    }

    @Test
    public void lifeTest2() {
        binder.bindType(PojoBeanTestBeanP.class).destroyMethod("doDestroy");
        DefaultBindInfoProviderAdapter<?> adapter = reference.get();
        Class<?> targetType = adapter.getSourceType() != null ? adapter.getSourceType() : adapter.getBindType();
        assert adapter.getDestroyMethod(targetType) != null;
    }

    @Test
    public void injectTest1() throws Exception {
        try {
            ignoreMatcher = aClass -> {
                return aClass != PojoBeanTestBeanP.class;
            };
            //
            BindInfo<?> valueInfo = new MockBindInfo();
            Provider<Object> valProvider = Provider.of("val");
            //
            //
            Field innerField = DefaultBindInfoProviderAdapter.class.getDeclaredField("injectProperty");
            innerField.setAccessible(true);
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass("net.hasor.core.info.ParamInfo");
            //
            {
                ApiBinder.InjectPropertyBindingBuilder<?> bindType = binder.bindType(PojoBeanTestBeanP.class);
                bindType.inject("abc1", Timestamp.class);
                bindType.injectValue("abc2", 123);
                bindType.inject("abc3", valProvider);
                bindType.inject("abc4", valueInfo);
            }
            //
            Field paramTypeField = aClass.getField("paramType");
            Field useProviderField = aClass.getField("useProvider");
            Field valueInfoField = aClass.getField("valueInfo");
            Field valueProviderField = aClass.getField("valueProvider");
            paramTypeField.setAccessible(true);
            useProviderField.setAccessible(true);
            valueInfoField.setAccessible(true);
            valueProviderField.setAccessible(true);
            Map<String, Object> propertys = (Map<String, Object>) innerField.get(reference.get());
            //
            {
                assert reference.get().getBindType() == PojoBeanTestBeanP.class;
                assert propertys.containsKey("abc1") &&//
                        propertys.containsKey("abc2") &&//
                        propertys.containsKey("abc3") && //
                        propertys.containsKey("abc4");
            }
            //
            assert paramTypeField.get(propertys.get("abc1")) == Date.class;
            assert ((DefaultBindInfoProviderAdapter) valueInfoField.get(propertys.get("abc1"))).getBindType() == Timestamp.class;
            //
            assert paramTypeField.get(propertys.get("abc2")) == Integer.TYPE;
            assert useProviderField.getBoolean(propertys.get("abc2"));
            assert ((Supplier) valueProviderField.get(propertys.get("abc2"))).get().equals(123);
            //
            assert paramTypeField.get(propertys.get("abc3")) == Object.class;
            assert useProviderField.getBoolean(propertys.get("abc3"));
            assert valProvider == valueProviderField.get(propertys.get("abc3"));
            assert "val".equals(((Supplier) valueProviderField.get(propertys.get("abc3"))).get());
            //
            assert paramTypeField.get(propertys.get("abc4")) == Method.class;
            assert !useProviderField.getBoolean(propertys.get("abc4"));
            assert valueInfoField.get(propertys.get("abc4")) == valueInfo;
        } finally {
            ignoreMatcher = null;
        }
    }

    @Test
    public void injectTest2() throws Exception {
        try {
            ignoreMatcher = aClass -> {
                return aClass != PojoBeanTestBeanC.class;
            };
            //
            BindInfo<?> valueInfo = new MockBindInfo();
            Provider<Object> valProvider = Provider.of("val");
            //
            //
            Field innerField = DefaultBindInfoProviderAdapter.class.getDeclaredField("constructorParams");
            innerField.setAccessible(true);
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass("net.hasor.core.info.ParamInfo");
            //
            {
                Constructor<PojoBeanTestBeanC> constructor = PojoBeanTestBeanC.class.getConstructor(Date.class, Integer.TYPE, Object.class, Method.class);
                ApiBinder.InjectConstructorBindingBuilder<PojoBeanTestBeanC> bindType = binder.bindType(PojoBeanTestBeanC.class).toConstructor(constructor);
                bindType.inject(0, Timestamp.class);
                bindType.injectValue(1, 123);
                bindType.inject(2, valProvider);
                bindType.inject(3, valueInfo);
            }
            //
            Field paramTypeField = aClass.getField("paramType");
            Field useProviderField = aClass.getField("useProvider");
            Field valueInfoField = aClass.getField("valueInfo");
            Field valueProviderField = aClass.getField("valueProvider");
            paramTypeField.setAccessible(true);
            useProviderField.setAccessible(true);
            valueInfoField.setAccessible(true);
            valueProviderField.setAccessible(true);
            Map<String, Object> propertys = (Map<String, Object>) innerField.get(reference.get());
            //
            {
                assert reference.get().getBindType() == PojoBeanTestBeanC.class;
                assert propertys.containsKey(0) &&//
                        propertys.containsKey(1) &&//
                        propertys.containsKey(2) && //
                        propertys.containsKey(3);
            }
            //
            assert paramTypeField.get(propertys.get(0)) == Date.class;
            assert ((DefaultBindInfoProviderAdapter) valueInfoField.get(propertys.get(0))).getBindType() == Timestamp.class;
            //
            assert paramTypeField.get(propertys.get(1)) == Integer.TYPE;
            assert useProviderField.getBoolean(propertys.get(1));
            assert ((Supplier) valueProviderField.get(propertys.get(1))).get().equals(123);
            //
            assert paramTypeField.get(propertys.get(2)) == Object.class;
            assert useProviderField.getBoolean(propertys.get(2));
            assert valProvider == valueProviderField.get(propertys.get(2));
            assert "val".equals(((Supplier) valueProviderField.get(propertys.get(2))).get());
            //
            assert paramTypeField.get(propertys.get(3)) == Method.class;
            assert !useProviderField.getBoolean(propertys.get(3));
            assert valueInfoField.get(propertys.get(3)) == valueInfo;
        } finally {
            ignoreMatcher = null;
        }
    }

    @Test
    public void aopTest1() {
        MethodInterceptor interceptor = invocation -> null;
        //
        try {
            binder.bindInterceptor("xxx", interceptor);
            AopBindInfoAdapter aopAdapter = (AopBindInfoAdapter) reference.get().getCustomerProvider().get();
            Field declaredField = AopBindInfoAdapter.class.getDeclaredField("interceptor");
            declaredField.setAccessible(true);
            assert declaredField.get(aopAdapter) == interceptor;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        //
        //
        try {
            Predicate<Class<?>> matcherClass = Matchers.anyClass();
            Predicate<Method> matcherMethod = Matchers.anyMethod();
            binder.bindInterceptor(matcherClass, matcherMethod, interceptor);
            AopBindInfoAdapter aopAdapter = (AopBindInfoAdapter) reference.get().getCustomerProvider().get();
            Field interceptorField = AopBindInfoAdapter.class.getDeclaredField("interceptor");
            Field matcherClassField = AopBindInfoAdapter.class.getDeclaredField("matcherClass");
            Field matcherMethodField = AopBindInfoAdapter.class.getDeclaredField("matcherMethod");
            interceptorField.setAccessible(true);
            matcherClassField.setAccessible(true);
            matcherMethodField.setAccessible(true);
            //
            assert interceptorField.get(aopAdapter) == interceptor;
            assert matcherClassField.get(aopAdapter) == matcherClass;
            assert matcherMethodField.get(aopAdapter) == matcherMethod;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void otherTest2() {
        assert binder.findClass(null).isEmpty();
        try {
            binder.findClass(ApiBinder.class);
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("requires package ranges");
        }
        assert binder.findClass(null, (String) null).isEmpty();
        assert !binder.findClass(ApiBinder.class, new String[] { "net.hasor.test.core.binder" }).isEmpty();
        assert binder.getSettings() != null;
    }
}
