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

/**
 * 使用 ID 方式进行依赖注入-based {@linkplain Qualifier qualifier}.
 * <p>Example usage:
 * <pre>
 *   public class Car {
 *     &#064;Inject <b>@ID("driver")</b> Seat driverSeat;
 *     &#064;Inject <b>@ID("passenger")</b> Seat passengerSeat;
 *     ...
 *   }</pre>
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ID {
    /** 如果同类型有多个注册可以使用该值进行区分。 */
    String value() default "";
}
