/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;
import net.hasor.core.AppContext;
import net.hasor.core.spi.AppContextAware;

/**
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2014年5月22日
 */
public class AopBindInfoAdapter implements MethodInterceptor, AppContextAware {
    private Predicate<Class<?>> matcherClass  = null;
    private Predicate<Method>   matcherMethod = null;
    private MethodInterceptor   interceptor   = null;

    public AopBindInfoAdapter(Predicate<Class<?>> matcherClass, Predicate<Method> matcherMethod, MethodInterceptor interceptor) {
        this.matcherClass = matcherClass;
        this.matcherMethod = matcherMethod;
        this.interceptor = interceptor;
    }

    public Predicate<Class<?>> getMatcherClass() {
        return matcherClass;
    }

    public Predicate<Method> getMatcherMethod() {
        return matcherMethod;
    }

    public Object invoke(final MethodInvocation invocation) throws Throwable {
        return this.interceptor.invoke(invocation);
    }

    public void setAppContext(AppContext appContext) {
        if (this.interceptor instanceof AppContextAware appContextAware) {
            appContextAware.setAppContext(appContext);
        }
    }
}
