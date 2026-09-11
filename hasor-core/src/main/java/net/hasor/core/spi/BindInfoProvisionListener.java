/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi;
import net.hasor.core.BindInfo;

/**
 * 新的 BindInfo。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-11-8
 */
public interface BindInfoProvisionListener extends java.util.EventListener {
    /**
     * 注入AppContext。
     * @param bindInfo 新对象的 BindInfo
     */
    void newBindInfo(BindInfo<?> bindInfo) throws Throwable;
}
