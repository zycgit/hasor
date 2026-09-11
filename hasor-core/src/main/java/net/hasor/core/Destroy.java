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
import net.hasor.core.ApiBinder.LifeBindingBuilder;

/**
 * 当容器销毁时调用的这个方法，如果{@link LifeBindingBuilder#destroyMethod(String)} 方法也定义了销毁方法则，注解方式优先于配置。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年7月28日
 * @see LifeBindingBuilder#destroyMethod(String)
 * @see javax.annotation.PreDestroy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface Destroy {
}
