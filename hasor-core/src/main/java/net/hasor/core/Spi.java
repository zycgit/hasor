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
import java.util.EventListener;
import javax.inject.Qualifier;

/**
 * 在一个实现类上标记该注解，用来表示实现了哪些 SPI
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2020年5月21日
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Spi {
    /** 指出实现了哪些 SPI，如果不设置值。那么会自动抽取实现的接口并将符合 SPI 规范的接口注册为 Hasor SPI。 */
    Class<? extends EventListener>[] value() default {};
}
