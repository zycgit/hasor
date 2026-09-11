/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render.json;
import java.io.Writer;
import java.util.Objects;
import com.google.gson.Gson;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** JSON renderer using Gson; independent of embedded server startup. */
public final class GsonRenderEngine implements RenderEngine {
    private final Gson gson;

    public GsonRenderEngine() {
        this(new Gson());
    }

    /** 复用业务配置好的 Gson 实例。 */
    public GsonRenderEngine(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) {
        Object value = invoker.get(Invoker.RETURN_DATA_KEY);
        this.gson.toJson(value, writer);
    }
}
