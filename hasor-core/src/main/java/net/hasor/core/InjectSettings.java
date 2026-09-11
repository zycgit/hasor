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
import javax.inject.Qualifier;
import net.hasor.core.spi.InjectMembers;

/**
 * 依赖注入，注入settings配置数据。请注意{@link InjectMembers}接口方式与注解方式互斥，且接口方式优先于注解方式。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016年07月18日
 * @see javax.inject.Qualifier
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface InjectSettings {
    /** 聚焦在某个 配置空间 中 */
    String ns() default "";

    /** 配置Key */
    String value();

    /** 默认值 */
    String defaultValue() default "";
}
