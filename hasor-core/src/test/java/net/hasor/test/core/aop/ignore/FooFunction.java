/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.aop.ignore;
/**
 * Bean测试
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年1月12日
 */
public interface FooFunction {
    default String fooCall(String string) {
        return "call back : " + string;
    }

    default String throwError(String sayMessage) {
        throw new RuntimeException("");
    }
}
