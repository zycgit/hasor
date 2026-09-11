/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.aop;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.test.core.aop.custom.ClassMyAopBean;
import net.hasor.test.core.aop.custom.MethodMyAopBean;
import net.hasor.test.core.aop.custom.MyAop;
import net.hasor.test.core.aop.custom.MyAopInterceptor;
import org.junit.Test;

public class AnnoTest {
    @Test
    public void aopTest2() {
        AppContext appContext = Hasor.create().build(apiBinder -> {
            //1.任意类
            Predicate<Class<?>> atClass = Matchers.anyClass();
            //2.有MyAop注解的方法
            Predicate<Method> atMethod = Matchers.annotatedWithMethod(MyAop.class);
            //3.让@MyAop注解生效
            apiBinder.bindInterceptor(atClass, atMethod, new MyAopInterceptor());
        });
        //
        MyAopInterceptor.resetInit();
        assert !MyAopInterceptor.isCalled();
        MethodMyAopBean myAopBean1 = appContext.getInstance(MethodMyAopBean.class);
        assert myAopBean1.fooCall("abc").equals("call back : abc");
        assert MyAopInterceptor.isCalled();
        //
        MyAopInterceptor.resetInit();
        assert !MyAopInterceptor.isCalled();
        ClassMyAopBean myAopBean2 = appContext.getInstance(ClassMyAopBean.class);
        assert myAopBean2.fooCall("abc").equals("call back : abc");
        assert MyAopInterceptor.isCalled();
    }
}
