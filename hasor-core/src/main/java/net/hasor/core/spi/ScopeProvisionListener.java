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

/**
 * Scope 注册监听器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-11-8
 */
public interface ScopeProvisionListener extends java.util.EventListener {
    /**
     * 发现新的作用域。
     * @param scopeName 新作用域名。
     * @param scopeSupplier 新作用域。
     */
    void newScope(String scopeName, Supplier<? extends Scope> scopeSupplier) throws Throwable;
}
