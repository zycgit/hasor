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
 * 请求调用链
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-24
 */
@FunctionalInterface
public interface InvokerChain {
    /**
     * 继续执行后续的请求过滤器链。
     * @param invoker 当前请求对象
     * @throws Throwable 请求过程中抛出的异常。
     */
    Object doNext(Invoker invoker) throws Throwable;
}
