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
public interface MetaInfo {
    /**
     * 获取元信息。
     * @param key 元信息 key
     * @return 返回元信息值
     */
    Object getMetaData(String key);

    /**
     * 设置元数据
     * @param key 元信息 key
     * @param value 元信息值
     */
    void setMetaData(String key, Object value);

    /**
     * 删除元数据
     * @param key 元信息 key
     */
    void removeMetaData(String key);
}
