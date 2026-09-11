/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.aop.custom;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;

public class MyAopInterceptor implements MethodInterceptor {
    private static boolean called  = false;
    private static boolean throwed = false;

    public static boolean isCalled() {
        return called;
    }

    public static boolean isThrowed() {
        return throwed;
    }

    public static void resetInit() {
        called = false;
        throwed = false;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        called = true;
        try {
            return invocation.proceed();
        } catch (Exception e) {
            throwed = true;
            throw e;
        }
    }
}
