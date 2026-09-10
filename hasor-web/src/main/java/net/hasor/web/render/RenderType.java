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
package net.hasor.web.render;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认使用的渲染器名字。
 * @version : 2020-02-29
 * @author 赵永春 (zyc@hasor.net)
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderType {
    /**
     * 默认使用的渲染器名字。
     * 根据渲染器名字推断 ContentType；显式 @Produces 注解优先。
     * @see net.hasor.web.render.RenderProcessor */
    String value() default "";

    /**
     * 默认使用的渲染器类型，与 value 行为不同的是。是否处理 ContentType 取决于 engineType 的实现。
     * @see net.hasor.web.render.RenderProcessor */
    Class<? extends RenderEngine> engineType() default DEFAULT.class;

    class DEFAULT implements RenderEngine {
        @Override
        public void process(RenderInvoker invoker, Writer writer) {
        }
    }
}
