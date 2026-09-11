/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.aop;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class AopBeanInterceptor implements MethodInterceptor {
    private final Map<String, List<String>> callInfo = new HashMap<>();

    public Map<String, List<String>> getCallInfo() {
        return callInfo;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        invocation.getMethod();
        invocation.getArguments();
        invocation.getThis();
        //
        String methodName = invocation.getMethod().getName();
        List<String> stringList = callInfo.get(methodName);
        if (stringList == null) {
            stringList = new ArrayList<>();
            callInfo.put(methodName, stringList);
        }
        //
        try {
            stringList.add("BEFORE");
            Object proceed = invocation.proceed();
            stringList.add("AFTER");
            return proceed;
        } catch (Exception e) {
            stringList.add("THROW");
            throw e;
        }
    }
}
