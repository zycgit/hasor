/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.annotation;
import java.lang.annotation.*;

/**
 * 标记在方法上用来设置 response 使用的 ContentType。如果没有配置该注解，那么 Hasor-web 会采用请求资源的后缀名，然后在 mime 中进行匹配。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-8-14
 */
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Produces {
    /** 指定的内容响应类型 */
    String value();
}
