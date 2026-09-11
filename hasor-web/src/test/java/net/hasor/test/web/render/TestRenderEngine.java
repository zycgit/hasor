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
import java.util.*;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSON;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;
import net.hasor.web.valid.ValidInvoker;

public class TestRenderEngine implements RenderEngine {
    private final Set<String> templateSet = new HashSet<>();

    public TestRenderEngine(List<String> templateSet) {
        this.templateSet.addAll(templateSet.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws Throwable {
        Map<String, Object> invokerData = new HashMap<>();
        invoker.keySet().stream().filter(s -> {                         //
            return !Invoker.ROOT_DATA_KEY.equalsIgnoreCase(s) &&        //
                    !ValidInvoker.VALID_DATA_KEY.equalsIgnoreCase(s) && //
                    !Invoker.REQUEST_KEY.equalsIgnoreCase(s) &&         //
                    !Invoker.RESPONSE_KEY.equalsIgnoreCase(s);          //
        }).peek(s -> {
            Object valueData = invoker.get(s);
            if (valueData == null) {
                invokerData.put(s, null);
            } else if (valueData instanceof String) {
                try {
                    invokerData.put(s, JSON.parseObject(valueData.toString()));
                } catch (Throwable e) {
                    invokerData.put(s, valueData);
                }
            } else {
                invokerData.put(s, valueData);
            }
        }).forEach(s -> {
            //
        });
        //
        invokerData.put("engine_renderTo", invoker.renderTo());
        invokerData.put("engine_viewType", invoker.renderType());
        //
        writer.write(JSON.toJSONString(invokerData));
    }

    @Override
    public boolean exist(String template) throws IOException {
        return templateSet.contains(template);
    }
}
