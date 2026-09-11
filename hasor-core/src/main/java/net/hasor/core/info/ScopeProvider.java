/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.util.function.Supplier;
import net.hasor.cobble.provider.Scope;

/**
 * 如果Bean配置了{@link Scope}，那么Hasor容器需要通过该接口获取到这个Scope。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年12月2日
 */
public interface ScopeProvider {
    /** 获取Provider对象，可以直接取得对象实例。 */
    Supplier[] getCustomerScopeProvider();
}
