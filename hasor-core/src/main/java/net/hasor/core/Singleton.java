/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core;
import java.lang.annotation.*;
import javax.inject.Scope;
import net.hasor.core.ApiBinder.ScopedBindingBuilder;

/**
 * 标记类型为单例模式，与 {@link Prototype} 为互斥关系，代码配置优先于注解。
 * 当 {@link Singleton} 和 {@link ImplBy} 组合使用时，标记在接口上的 Singleton 注解会覆盖 ImplBy 指定的那个实现。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年7月28日
 * @see ScopedBindingBuilder#asEagerSingleton()
 * @see javax.inject.Singleton
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Singleton {
}
