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
 * Cookie的中的值
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-5-9
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Documented
@WebParameter
public @interface CookieParameter {
    /** 参数名称。 */
    String value();
}
