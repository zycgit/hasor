/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core;
/**
 * 应用程序事件监听器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-7-10
 */
@FunctionalInterface
public interface EventListener<T> extends java.util.EventListener {
    /**
     * 处理事件的处理方法，参数是要处理的事件。
     * @param event 事件类型
     * @param eventData 事件参数
     * @throws Throwable 执行事件期间引发的异常。
     */
    void onEvent(String event, T eventData) throws Throwable;
}
