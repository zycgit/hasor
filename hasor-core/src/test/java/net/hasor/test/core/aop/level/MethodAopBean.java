/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.aop.level;
import net.hasor.cobble.dynamic.Aop;
import net.hasor.test.core.aop.custom.MyAopInterceptor;

/**
 * 方法级别
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-1-3
 */
public class MethodAopBean {
    @Aop(MyAopInterceptor.class)
    public String fooCall(String string) {
        System.out.println("fooCall");
        return "call back : " + string;
    }
}
