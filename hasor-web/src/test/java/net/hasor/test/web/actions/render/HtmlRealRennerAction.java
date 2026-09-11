/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.render;
import java.util.HashMap;
import net.hasor.web.annotation.*;
import net.hasor.web.render.RenderInvoker;

public class HtmlRealRennerAction {
    @Post
    public Object testProduces1(RenderInvoker invoker) {
        invoker.renderTo("html", "/my/my.html");
        return new HashMap<String, String>() {{
            put("data", "hello");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }

    @Get
    public Object testProduces2(RenderInvoker invoker) {
        invoker.renderTo("json", "/my/my.data");
        return new HashMap<String, String>() {{
            put("data", "word");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }

    @Head
    public Object testProduces3(RenderInvoker invoker) {
        invoker.renderTo("/my/my.abc");
        invoker.getHttpResponse().setContentType("abcdefg");
        return new HashMap<String, String>() {{
            put("data", "word");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }

    @Options
    @Produces("html")
    public Object testProduces4(RenderInvoker invoker) {
        invoker.renderTo("/my/my.abc");
        invoker.getHttpResponse().setContentType("abcdefg");    // @Produces 和 setContentType 同时配置，会导致三次 setContentType（最后一次值为 invoker.viewType()）
        return new HashMap<String, String>() {{
            put("data", "word");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }

    @Put
    @Produces("html")
    public Object testProduces5(RenderInvoker invoker) {
        invoker.renderTo("json", "/my/my.abc"); // 报错，已经配置了 @Produces 注解不可以修改 viewType
        return new HashMap<String, String>() {{
            put("data", "word");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }
}
