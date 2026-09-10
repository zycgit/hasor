/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.config;
import java.lang.annotation.*;

/** Declares that a method creates a bean managed by Hasor. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {
    /** Bean name. The factory method name is used when this value is empty. */
    String value() default "";

    /** Whether the bean is a singleton. */
    boolean singleton() default true;

    /** Optional initialization method on the returned bean. */
    String initMethod() default "";

    /** Optional destruction method on the returned bean. */
    String destroyMethod() default "";
}
