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
 * 请求过滤器，相当于 {@link javax.servlet.Filter} 同等作用。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
@FunctionalInterface
public interface InvokerFilter {
    /**
     * 初始化过滤器
     * @param config 配置信息
     * @throws Throwable 初始化过程中发生异常。
     */
    default void init(InvokerConfig config) throws Throwable {
    }

    /**
     * 指定过滤器
     * @param invoker 当前请求信息
     * @param chain 过滤器链
     * @throws Throwable 执行过滤器中发生的异常。
     */
    Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable;

    /** 销毁过滤器。 */
    default void destroy() {
    }
}
