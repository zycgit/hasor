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
import java.util.ArrayList;
import java.util.List;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;
import net.hasor.core.AppContext;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-13
 */
class AopChainInvocation implements MethodInvocation {
    private MethodInterceptor[] beforeInterceptor = null;
    private MethodInvocation    invocation        = null;
    private int                 index             = -1;

    public AopChainInvocation(AppContext appContext, List<Class<? extends MethodInterceptor>> interTypeList, MethodInvocation invocation) {
        List<MethodInterceptor> beforeList = new ArrayList<>();
        for (Class<? extends MethodInterceptor> interType : interTypeList) {
            if (interType != null) {
                beforeList.add(appContext.getInstance(interType));
            }
        }
        this.beforeInterceptor = beforeList.toArray(new MethodInterceptor[0]);
        this.invocation = invocation;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        index++;
        if (index < beforeInterceptor.length) {
            return beforeInterceptor[index].invoke(this);
        } else {
            return invocation.proceed();
        }
    }

    public Object[] getArguments() {
        return invocation.getArguments();
    }

    public Object proceed() throws Throwable {
        return this.invoke(this.invocation);
    }

    public Object getThis() {
        return invocation.getThis();
    }

    @Override
    public boolean isProxy() {
        return this.invocation.isProxy();
    }

    public Method getMethod() {
        return invocation.getMethod();
    }
}
