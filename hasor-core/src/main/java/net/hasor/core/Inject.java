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
import net.hasor.core.spi.InjectMembers;

/**
 * 依赖注入。请注意{@link InjectMembers}接口方式与注解方式互斥，且接口方式优先于注解方式。
 * <p>如果没有配置“value”参数那么将会使用“{@code AppContext.getInstance(class)}”方式进行依赖注入。</p>
 * <p>如果了配置value属性那么将会根据“byType”参数决定注入方式。
 * <ul>
 * <li>“byType=Type.ByID”使用“{@code AppContext.getInstance(String)}”方式进行依赖注入。</li>
 * <li>“byType=Type.ByName”使用“{@code AppContext.findBindingBean(withName, bindType)}”方式进行依赖注入。</li>
 * </ul></p>
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年7月28日
 * @see net.hasor.core.ID
 * @see javax.inject.Inject
 * @see javax.inject.Named
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface Inject {
    /** 如果同类型有多个注册可以使用该值进行区分。 */
    String value() default "";

    /**
     * 区分注入Bean的方式<ul>
     * <li>“byType=Type.ByID”使用“{@code AppContext.getInstance(String)}”方式进行依赖注入。</li>
     * <li>(默认配置)“byType=Type.ByName”使用“{@code AppContext.findBindingBean(withName, bindType)}”方式进行依赖注入。</li>
     * </ul>
     */
    Type byType() default Type.ByName;
}
