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
 * Hasor模块，任何扩展功能都是通过 Module 接口进行，这是 Hasor 开发的主要入口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-3-20
 */
@FunctionalInterface
public interface Module {
    /** Bind metadata indicating that this type has already executed its Module initialization. */
    String MODULE_INSTALLED = "hasor.module.installed";

    /** 表示放弃后续 onStart/onStop 的执行 */
    final class IgnoreModuleException extends RuntimeException {
    }

    /**
     * 初始化过程，注意：apiBinder 参数只能在 loadModule 阶段中使用。
     * 如果只要不抛错，后续 onStart/onStop 都会被调用
     * @throws Throwable init异常抛出
     * @throws IgnoreModuleException 如果抛出该类型异常则表示放弃后续 onStart/onStop 的执行，module 的加载仍然继续
     */
    void loadModule(ApiBinder apiBinder) throws Throwable;

    /**
     * 启动过程。
     * @param appContext appContext
     * @throws Throwable init异常抛出
     */
    default void onStart(AppContext appContext) throws Throwable {
    }

    /**
     * 终止过程。
     * @param appContext appContext
     * @throws Throwable init异常抛出
     */
    default void onStop(AppContext appContext) throws Throwable {
    }
}
