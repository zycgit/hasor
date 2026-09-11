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
 * 当 AppContext 创建这个Bean时。容器会调用Bean实现的这个接口方法，将Bean的Info注入进来。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-11-8
 */
@FunctionalInterface
public interface BindInfoAware {
    /**
     * 注入BindInfo。
     * @param bindInfo 注入的BindInfo。
     */
    void setBindInfo(BindInfo<?> bindInfo);
}
