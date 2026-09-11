/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 控制器映射的地址。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-3-26
 */
@Repeatable(MappingToGroup.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingTo {
    /** 请求地址 */
    String[] value();
}
