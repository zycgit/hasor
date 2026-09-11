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
import net.hasor.core.ApiBinder.LinkedBindingBuilder;

/**
 * <p>标记当前类型的具体实现。</p>
 * 如果通过 {@link ApiBinder#bindType(Class)} 的 to 方式也指定了实现类，那么代码方式会优先注解配置。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年7月28日
 * @see LinkedBindingBuilder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface ImplBy {
    /** 实现类。 */
    Class<?> value();
}
