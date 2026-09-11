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
import net.hasor.core.ApiBinder.InjectConstructorBindingBuilder;

/**
 * 如果通过{@link InjectConstructorBindingBuilder}接口配置会覆盖注解配置。
 * 如果在该类上出现多个 {@link ConstructorBy} 注解配置，那么将会按照 构造方法参数个数排序取参数数量最少的那个构造方法。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2018年9月21日
 * @see InjectConstructorBindingBuilder
 * @see javax.inject.Inject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
@Documented
public @interface ConstructorBy {
}
