/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render.text;
import java.io.IOException;
import java.io.Writer;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** Emits an already formatted value without interpreting it as JSON or a template path. */
public final class TextRenderEngine implements RenderEngine {
    @Override
    public void process(RenderInvoker invoker, Writer writer) throws IOException {
        Object value = invoker.get(Invoker.RETURN_DATA_KEY);
        if (value != null) {
            writer.write(value.toString());
        }
    }
}
