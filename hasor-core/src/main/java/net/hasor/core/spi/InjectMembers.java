/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi;
import net.hasor.core.AppContext;
import net.hasor.core.Inject;

/**
 * 初始化注入接口。Hasor 的 Ioc 是通过递归的方式实现，版本中要想实依赖注入必须要实现 InjectMembers接口。
 * 请注意：{@link Inject}注解方式和接口方式互斥，且接口方式优先于注解方式。
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2014-5-10
 */
@FunctionalInterface
public interface InjectMembers {
    /**
     * 执行注入
     * @param appContext appContext对象
     */
    void doInject(AppContext appContext) throws Throwable;
}
