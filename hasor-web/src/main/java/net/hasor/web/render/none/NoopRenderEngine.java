/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render.none;
import java.io.Writer;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** Deliberately leaves the response body and headers untouched. */
public final class NoopRenderEngine implements RenderEngine {
    @Override
    public void process(RenderInvoker invoker, Writer writer) {
    }
}
