/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.Writer;

/** Test fixture for render engines that keeps the response type explicit. */
public final class RenderEngineTestSupport {
    private RenderEngineTestSupport() {
    }

    public static RenderEngine renderEngine(String contentType, RenderAction action) {
        return new RenderEngine() {
            @Override
            public String defaultContentType() {
                return contentType;
            }

            @Override
            public void process(RenderInvoker invoker, Writer writer) throws Throwable {
                action.process(invoker, writer);
            }
        };
    }

    @FunctionalInterface
    public interface RenderAction {
        void process(RenderInvoker invoker, Writer writer) throws Throwable;
    }
}
