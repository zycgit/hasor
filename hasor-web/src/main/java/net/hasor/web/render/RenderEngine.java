/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.IOException;
import java.io.Writer;

/**
 * 渲染引擎
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016年1月3日
 */
public interface RenderEngine {
    /** 执行模版引擎 */
    void process(RenderInvoker invoker, Writer writer) throws Throwable;

    /** exist 的作用是用来在 process 执行之前，让渲染器检查一下，要执行的 模板是否存在。如果不存在就不会执行 process */
    default boolean exist(String template) throws IOException {
        return true;
    }
}
