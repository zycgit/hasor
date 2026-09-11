/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render.json;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** Optional Jackson 2 JSON renderer. */
public final class JacksonRenderEngine implements RenderEngine {
    private final ObjectMapper mapper;

    public JacksonRenderEngine() {
        this(new ObjectMapper());
    }

    /** 复用业务配置好的 ObjectMapper；应在处理请求前完成配置。 */
    public JacksonRenderEngine(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws IOException {
        Object value = invoker.get(Invoker.RETURN_DATA_KEY);
        writer.write(this.mapper.writeValueAsString(value));
    }
}
