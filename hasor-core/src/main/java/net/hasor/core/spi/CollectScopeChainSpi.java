/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi;
import java.util.function.Supplier;
import net.hasor.cobble.provider.Scope;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;

/**
 * 查找目标使用的作用域
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-01-06
 */
public interface CollectScopeChainSpi extends java.util.EventListener {
    /**
     * 查找目标使用的作用域。
     * @param bindInfo 正在被创建的 BindInfo
     * @param appContext 容器对象
     * @param suppliers 已经找到了的作用域
     */
    Supplier<Scope>[] collectScope(BindInfo<?> bindInfo, AppContext appContext, Supplier<Scope>[] suppliers) throws Throwable;

    /**
     * 查找目标使用的作用域。
     * @param targetType 正在被创建的 类型
     * @param appContext 容器对象
     * @param suppliers 已经找到了的作用域
     */
    Supplier<Scope>[] collectScope(Class<?> targetType, AppContext appContext, Supplier<Scope>[] suppliers) throws Throwable;
}
