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