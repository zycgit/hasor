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
import net.hasor.web.annotation.Any;
import net.hasor.web.render.RenderInvoker;

public class ParentLayoutHtmlAction {
    @Any
    public Object testProduces(RenderInvoker invoker) {
        invoker.renderTo("html", "/my/abc/my.html");
        return new HashMap<String, String>() {{
            put("data", "hello");
            put("renderTo", invoker.renderTo());
            put("viewType", invoker.renderType());
        }};
    }
}
