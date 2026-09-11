/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.render;
import net.hasor.web.annotation.*;
import net.hasor.web.render.RenderInvoker;

@MappingTo("/abc.do")
@MappingTo("/def.do")
public class HtmlProduces {
    @Post
    @Produces("test/html")
    public void testProduces1() {
    }

    @Get
    @Produces("text/javacc_jj")
    public void testProduces2(RenderInvoker invoker) {
        invoker.renderTo("/my/my.html");
    }

    @Put
    public void testProduces3(RenderInvoker invoker) {
        invoker.renderTo("/my/my.html");
    }
}
