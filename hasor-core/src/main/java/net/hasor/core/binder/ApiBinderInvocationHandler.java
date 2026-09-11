/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class ApiBinderInvocationHandler implements InvocationHandler {
    private final Map<Class<?>, Object> supportMap;

    protected Map<Class<?>, Object> supportMap() {
        return Collections.unmodifiableMap(this.supportMap);
    }

    public ApiBinderInvocationHandler(Map<Class<?>, Object> supportMap) {
        this.supportMap = supportMap;
        for (Map.Entry<Class<?>, Object> entry : supportMap.entrySet()) {
            if (entry.getValue() == null) {
                throw new UnsupportedOperationException("this method is not support -> " + entry.getKey());
            }
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getName().equals("toString")) {
            return proxyToString();
        }
        //
        Class<?> declaringClass = method.getDeclaringClass();
        Object target = this.supportMap.get(declaringClass);
        //
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    private Object proxyToString() {
        StringBuilder builder = new StringBuilder();
        builder = builder.append("count = ").append(this.supportMap.size()).append(" - [");
        for (Class<?> face : this.supportMap.keySet()) {
            builder = builder.append(face.getName()).append(",");
        }
        if (builder.charAt(builder.length() - 1) == ',') {
            builder = builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("]");
        return builder.toString();
    }
}
