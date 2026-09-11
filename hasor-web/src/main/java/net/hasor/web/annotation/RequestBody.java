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
 * 查询参数Body
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Documented
@WebParameter
public @interface RequestBody {
}
