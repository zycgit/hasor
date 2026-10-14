/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.Produces;
import net.hasor.web.render.json.JacksonRenderEngine;
import net.hasor.web.render.text.TextRenderEngine;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RenderContentTypeTest extends AbstractTest {
    @RenderType(engineType = JacksonRenderEngine.class)
    public static class JsonAction {
        @Get
        public Map<String, String> get() {
            return Map.of("name", "hasor");
        }
    }

    @RenderType(engineType = TextRenderEngine.class)
    public static class TextAction {
        @Get
        public String get() {
            return "hasor";
        }
    }

    @RenderType(engineType = VendorRenderEngine.class)
    public static class VendorAction {
        @Get
        public String get() {
            return "ignored";
        }
    }

    @RenderType(engineType = JacksonRenderEngine.class)
    public static class ExplicitAction {
        @Get
        @Produces("application/problem+json")
        public Map<String, String> get() {
            return Map.of("name", "hasor");
        }
    }

    @RenderType(engineType = JacksonRenderEngine.class)
    public static class ResponseAction {
        @Get
        public Map<String, String> get(Invoker invoker) {
            invoker.getHttpResponse().setContentType("application/vnd.controller+json");
            return Map.of("name", "hasor");
        }
    }

    @RenderType(engineType = JacksonRenderEngine.class)
    public static class InvokerAction {
        @Get
        public Map<String, String> get(Invoker invoker) {
            invoker.contentType("application/vnd.invoker+json");
            return Map.of("name", "hasor");
        }
    }

    @RenderType(engineType = JacksonRenderEngine.class)
    public static class ProducedInvokerAction {
        @Get
        @Produces("application/problem+json")
        public Map<String, String> get(Invoker invoker) {
            invoker.contentType("application/vnd.invoker+json");
            return Map.of("name", "hasor");
        }
    }

    @RenderType(engineType = UntypedRenderEngine.class)
    public static class UntypedAction {
        @Get
        public String get() {
            return "ignored";
        }
    }

    @RenderType(engineType = TemplateRenderEngine.class)
    public static class TemplateAction {
        @Get
        public String get(RenderInvoker invoker) {
            invoker.layoutDisable();
            invoker.renderTo("/view.html");
            return "ignored";
        }
    }

    @RenderType(engineType = TemplateRenderEngine.class)
    public static class LayoutAction {
        @Get
        public String get(RenderInvoker invoker) {
            invoker.layoutEnable();
            invoker.renderTo("/view.html");
            return "ignored";
        }
    }

    public static class VendorRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return "application/vnd.hasor+text";
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            writer.write("vendor");
        }
    }

    public static class UntypedRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return null;
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            writer.write("untyped");
        }
    }

    public static class TemplateRenderEngine implements RenderEngine {
        @Override
        public String defaultContentType() {
            return "text/x-template";
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            writer.write("template");
        }
    }

    @Test
    public void jacksonEngineDeclaresJsonResponse() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("{\"name\":\"hasor\"}", call(JsonAction.class, contentTypes));
        assertEquals(Set.of("application/json"), contentTypes);
    }

    @Test
    public void textEngineDeclaresPlainTextResponse() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("hasor", call(TextAction.class, contentTypes));
        assertEquals(Set.of("text/plain"), contentTypes);
    }

    @Test
    public void customEngineCanDeclareItsResponseType() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("vendor", call(VendorAction.class, contentTypes));
        assertEquals(Set.of("application/vnd.hasor+text"), contentTypes);
    }

    @Test
    public void producesOverridesEngineDefault() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("{\"name\":\"hasor\"}", call(ExplicitAction.class, contentTypes));
        assertEquals(Set.of("application/problem+json"), contentTypes);
    }

    @Test
    public void controllerResponseTypeOverridesEngineDefault() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("{\"name\":\"hasor\"}", call(ResponseAction.class, contentTypes));
        assertEquals(Set.of("application/vnd.controller+json"), contentTypes);
    }

    @Test
    public void invokerResponseTypeOverridesEngineDefault() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("{\"name\":\"hasor\"}", call(InvokerAction.class, contentTypes));
        assertEquals(Set.of("application/vnd.invoker+json"), contentTypes);
    }

    @Test
    public void existingResponseTypeRemainsAuthoritative() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("{\"name\":\"hasor\"}", call(ProducedInvokerAction.class, contentTypes));
        assertEquals(Set.of("application/problem+json"), contentTypes);
    }

    @Test
    public void interceptorResponseTypeOverridesEngineDefault() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(JsonAction.class);
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public void postHandle(Invoker invoker, Object result) {
                    invoker.contentType("application/vnd.interceptor+json");
                }
            });
        })) {
            String result = mockAndCallHttp("GET", "http://localhost/test", app, contentTypes, null);
            assertEquals("{\"name\":\"hasor\"}", result);
            assertEquals(Set.of("application/vnd.interceptor+json"), contentTypes);
        }
    }

    @Test
    public void untypedEngineUsesBinaryFallback() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("untyped", call(UntypedAction.class, contentTypes));
        assertEquals(Set.of("application/octet-stream"), contentTypes);
    }

    @Test
    public void templateEngineDeclaresItsResponseType() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("template", call(TemplateAction.class, contentTypes));
        assertEquals(Set.of("text/x-template"), contentTypes);
    }

    @Test
    public void layoutEngineDeclaresItsResponseType() throws Throwable {
        Set<String> contentTypes = new HashSet<>();
        assertEquals("template", call(LayoutAction.class, contentTypes));
        assertEquals(Set.of("text/x-template"), contentTypes);
    }

    private String call(Class<?> actionType, Set<String> contentTypes) throws Throwable {
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(actionType);
        })) {
            return mockAndCallHttp("GET", "http://localhost/test", app, contentTypes, null);
        }
    }
}
