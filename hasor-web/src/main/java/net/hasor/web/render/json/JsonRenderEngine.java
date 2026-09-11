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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.hasor.core.Singleton;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/**
 * 一个基于 Fastjson 的JSON渲染器。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-20
 */
@Singleton
public class JsonRenderEngine implements RenderEngine {
    private final SerializeConfig     config;
    private final SerializerFeature[] features;

    public JsonRenderEngine() {
        this(SerializeConfig.getGlobalInstance());
    }

    /** 使用指定配置，不修改 Fastjson 全局配置。 */
    public JsonRenderEngine(SerializeConfig config, SerializerFeature... features) {
        this.config = Objects.requireNonNull(config, "config");
        this.features = Objects.requireNonNull(features, "features").clone();
        for (SerializerFeature feature : this.features) {
            Objects.requireNonNull(feature, "feature");
        }
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws Throwable {
        Object obj = invoker.get(Invoker.RETURN_DATA_KEY);
        writer.write(JSON.toJSONString(obj, this.config, this.features));
    }
}
