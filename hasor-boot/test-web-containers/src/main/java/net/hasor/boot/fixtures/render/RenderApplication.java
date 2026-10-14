/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.render;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import net.hasor.web.Invoker;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.Head;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.Produces;
import net.hasor.web.render.RedirectTo;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;
import net.hasor.web.render.RenderType;

public class RenderApplication implements WebModule {
    @MappingTo("/render/redirect-default")
    public static class DefaultRedirectAction {
        @Get
        @RedirectTo
        public String get() {
            return "/render/text";
        }
    }

    @MappingTo("/render/redirect-permanent")
    @RedirectTo(301)
    public static class PermanentRedirectAction {
        @Get
        public String get() {
            return "/render/text";
        }
    }

    @MappingTo("/render/redirect-override")
    @RedirectTo(301)
    public static class OverrideRedirectAction {
        @Get
        @RedirectTo(302)
        public String get() {
            return "/render/text";
        }
    }

    @Override
    public void loadModule(WebApiBinder binder) {
        binder.setResponseCharacter("UTF-8");
    }

    @MappingTo("/render/object")
    public static class ObjectAction {
        @Get
        @Head
        public Map<String, String> get() {
            return Map.of("name", "中文");
        }
    }

    @MappingTo("/render/list")
    public static class ListAction {
        @Get
        public List<Integer> get() {
            return List.of(1, 2);
        }
    }

    @MappingTo("/render/pojo")
    public static class PojoAction {
        public record Item(String name, int count) {
        }

        @Get
        public Item get() {
            return new Item("中文", 2);
        }
    }

    @MappingTo("/render/text")
    public static class TextAction {
        @Get
        public String get() {
            return "中文";
        }
    }

    @MappingTo("/render/raw")
    @RenderType("json")
    public static class RawAction {
        @Get
        @RenderType("text")
        @Produces("application/json")
        public String get() {
            return "{\"name\":\"中文\"}";
        }
    }

    @MappingTo("/render/media")
    public static class MediaAction {
        @Get
        @Produces("application/problem+json")
        public Map<String, String> get() {
            return Map.of("name", "中文");
        }
    }

    @MappingTo("/render/engine-type")
    @RenderType(engineType = DirectRenderEngine.class)
    public static class DirectRenderAction {
        @Get
        public String get() {
            return "ignored";
        }
    }

    public static class DirectRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return "application/vnd.hasor+json";
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws IOException {
            writer.write("{\"source\":\"engine\"}");
        }
    }

    @MappingTo("/render/charset")
    public static class CharsetAction {
        @Get
        @RenderType("text")
        @Produces("text/plain;charset=GB18030")
        public String get() {
            return "中文";
        }
    }

    @MappingTo("/render/null")
    public static class NullAction {
        @Get
        public Object get() {
            return null;
        }
    }

    @MappingTo("/render/api-charset")
    public static class ApiCharsetAction {
        @Get
        public String get(Invoker invoker) {
            invoker.getHttpResponse().setCharacterEncoding("GB18030");
            return "中文";
        }
    }

    @MappingTo("/render/none")
    public static class NoneAction {
        @Get
        @RenderType("none")
        public String get() {
            return "must-not-write";
        }
    }

    public static class RequestEncodingModule implements WebModule {
        @Override
        public void loadModule(WebApiBinder binder) {
            binder.setRequestCharacter("GB18030");
        }
    }

    public static class ResponseEncodingModule implements WebModule {
        @Override
        public void loadModule(WebApiBinder binder) {
            binder.setEncodingCharacter("GB18030", "UTF-16LE");
        }
    }

    @MappingTo("/render/void")
    public static class VoidAction {
        @Get
        public void get() {
        }
    }

    @MappingTo("/render/written")
    public static class WrittenAction {
        @Get
        public String get(Invoker invoker) throws IOException {
            invoker.getHttpResponse().getWriter().write("manual");
            return "must-not-append";
        }
    }

    @MappingTo("/render/stream")
    public static class StreamAction {
        @Get
        public String get(Invoker invoker) throws IOException {
            invoker.getHttpResponse().getOutputStream().write(new byte[] { 65, 66 });
            return "must-not-append";
        }
    }

    @MappingTo("/render/no-content")
    public static class NoContentAction {
        @Get
        public String get(Invoker invoker) {
            invoker.getHttpResponse().setStatus(204);
            return "must-not-append";
        }
    }

    @MappingTo("/render/cache")
    public static class CacheAction {
        @Get
        public String get(Invoker invoker) {
            invoker.getHttpResponse().setHeader("Cache-Control", "private");
            return "ok";
        }
    }

    public static class CustomJsonRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return "application/json";
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws IOException {
            writer.write("\"custom-json\"");
        }
    }

    public static class CustomRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return null;
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws IOException {
            writer.write("custom-engine");
        }
    }

    public static class CustomModule implements WebModule {
        @Override
        public void loadModule(WebApiBinder binder) {
            binder.setResponseCharacter("UTF-8");
            binder.addRender("json").toProvider(CustomJsonRenderEngine::new);
            binder.addRender("custom").toProvider(CustomRenderEngine::new);
        }
    }
}
