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
 * 表示一个 bean 的配置信息。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-3-17
 */
public interface BindInfo<T> extends MetaInfo {
    /** @return 绑定的ID */
    String getBindID();

    /** @return 为类型绑定的名称。 */
    String getBindName();

    /** @return 获取注册的类型 */
    Class<T> getBindType();
}
