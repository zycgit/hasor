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
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.function.Supplier;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** Optional Fastjson 2 JSON renderer. */
public final class Fastjson2RenderEngine implements RenderEngine {
    private final Supplier<JSONWriter.Context> contexts;

    public Fastjson2RenderEngine() {
        this(JSONFactory.getDefaultObjectWriterProvider());
    }

    /** 共享序列化器注册表，每次渲染创建独立上下文。 */
    public Fastjson2RenderEngine(ObjectWriterProvider provider, JSONWriter.Feature... features) {
        Objects.requireNonNull(provider, "provider");
        JSONWriter.Feature[] snapshot = Objects.requireNonNull(features, "features").clone();
        for (JSONWriter.Feature feature : snapshot) {
            Objects.requireNonNull(feature, "feature");
        }

        this.contexts = () -> new JSONWriter.Context(provider, snapshot);
    }

    /** 支持日期格式、过滤器等配置；工厂须为每次调用提供独立上下文，返回 null 时使用默认序列化。 */
    public Fastjson2RenderEngine(Supplier<JSONWriter.Context> contexts) {
        this.contexts = Objects.requireNonNull(contexts, "contexts");
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws IOException {
        Object value = invoker.get(Invoker.RETURN_DATA_KEY);
        JSONWriter.Context context = this.contexts.get();
        writer.write(context == null ? JSON.toJSONString(value) : JSON.toJSONString(value, context));
    }
}