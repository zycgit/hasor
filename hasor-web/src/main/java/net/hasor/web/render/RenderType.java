/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认使用的渲染器名字。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-02-29
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderType {
    /**
     * 默认使用的渲染器名字。
     * 根据渲染器名字推断 ContentType；显式 @Produces 注解优先。
     * @see net.hasor.web.render.RenderProcessor
     */
    String value() default "";

    /**
     * 默认使用的渲染器类型，响应类型由 engineType 的 defaultContentType 声明。
     * 已写入响应的类型优先；响应尚未设置时，Invoker.contentType 设置的类型也优先。
     * 未声明默认响应类型的引擎继续使用框架兜底类型。
     * @see RenderEngine#defaultContentType()
     * @see net.hasor.web.render.RenderProcessor
     */
    Class<? extends RenderEngine> engineType() default DEFAULT.class;

    class DEFAULT implements RenderEngine {
        @Override
        public String defaultContentType() {
            return null;
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) {
        }
    }
}
