/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
/**
 * invoker 扩展接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
@FunctionalInterface
public interface InvokerCreator {
    /**
     * 创建 {@link Invoker} 扩展
     * @param invoker 原始的 Invoker
     */
    Invoker createExt(Invoker invoker);
}
