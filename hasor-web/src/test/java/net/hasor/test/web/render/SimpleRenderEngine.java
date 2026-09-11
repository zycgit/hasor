/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.render;
import java.io.IOException;
import java.io.Writer;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-08
 */
public class SimpleRenderEngine implements RenderEngine {
    private boolean process;
    private boolean exist;

    public boolean isProcess() {
        return process;
    }

    public boolean isExist() {
        return exist;
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws Throwable {
        this.process = true;
    }

    @Override
    public boolean exist(String template) throws IOException {
        this.exist = true;
        return false;
    }
}
