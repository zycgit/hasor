/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Any;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;
import net.hasor.web.render.RenderType;
import net.hasor.web.wrap.InvokerWrap;
import org.junit.Test;
import static org.junit.Assert.*;

public class SkipRenderTest extends AbstractTest {
    public static class SkippedAction {
        private final AtomicInteger calls = new AtomicInteger();

        @Any
        @RenderType(engineType = UnexpectedRenderEngine.class)
        public String execute(RenderInvoker invoker) {
            this.calls.incrementAndGet();
            if (invoker.getHttpRequest().getParameter("view") != null) {
                invoker.renderTo("text", "/view.txt");
            }
            if (invoker.getHttpRequest().getParameter("controller") != null) {
                InvokerWrap wrapper = new InvokerWrap(invoker);
                wrapper.setSkipRender();
                wrapper.setSkipRender();
                assertTrue(wrapper.isSkipRender());
                assertTrue(invoker.isSkipRender());
            }
            return "controller result";
        }
    }

    public static class UnexpectedRenderEngine implements RenderEngine {
        private static final AtomicInteger creations = new AtomicInteger();

        public UnexpectedRenderEngine() {
            creations.incrementAndGet();
        }

        @Override
        public void process(RenderInvoker invoker, Writer writer) {
            throw new AssertionError("Skipped rendering must not invoke its engine");
        }
    }

    @Test
    public void filterAndControllerCanSkipRenderingWithoutLosingTheResult() throws Throwable {
        UnexpectedRenderEngine.creations.set(0);
        SkippedAction action = new SkippedAction();
        List<Object> results = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(action);
            binder.filter("/*").through((invoker, chain) -> {
                if (invoker.getHttpRequest().getParameter("controller") == null) {
                    invoker.setSkipRender();
                }
                Object result = chain.doNext(invoker);
                assertEquals(result, invoker.get(Invoker.RETURN_DATA_KEY));
                results.add(result);
                return result;
            });
        })) {
            for (String query : List.of("", "?view=true")) {
                assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test" + query, app));
            }
            assertEquals(0, UnexpectedRenderEngine.creations.get());
            for (String query : List.of("?controller=true", "?controller=true&view=true")) {
                assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test" + query, app));
            }
            assertEquals(0, UnexpectedRenderEngine.creations.get());
            assertEquals(4, action.calls.get());
            assertEquals(List.of("controller result", "controller result", "controller result", "controller result"), results);
        }
    }

    public static class ConditionalAction {
        private final AtomicInteger calls = new AtomicInteger();

        @Any
        @RenderType("text")
        public String execute(Invoker invoker) {
            this.calls.incrementAndGet();
            if (invoker.getHttpRequest().getParameter("skip") != null) {
                invoker.setSkipRender();
            }
            return "body";
        }
    }

    @Test
    public void skippingOneRequestDoesNotAffectTheNextRequest() throws Throwable {
        ConditionalAction action = new ConditionalAction();
        AtomicInteger renders = new AtomicInteger();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(action);
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                renders.incrementAndGet();
                writer.write(invoker.get(Invoker.RETURN_DATA_KEY).toString());
            });
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test?skip=true", app));
            assertEquals("body", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(2, action.calls.get());
            assertEquals(1, renders.get());
        }
    }

    public static class FailedAction {
        private final IOException failure = new IOException("controller failure");

        @Any
        public Object execute(Invoker invoker) throws IOException {
            invoker.setSkipRender();
            throw this.failure;
        }
    }

    @Test
    public void skippingRenderingDoesNotSwallowControllerFailures() throws Throwable {
        FailedAction action = new FailedAction();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(action);
        })) {
            try {
                this.mockAndCallHttp("GET", "http://localhost/test", app);
                fail("The controller failure must propagate");
            } catch (ExecutionException expected) {
                assertSame(action.failure, expected.getCause());
            }
        }
    }

    public static class OverrideAction {
        @Any
        @RenderType("text")
        public Object execute(RenderInvoker invoker) {
            assertEquals("TEXT", invoker.renderType());
            assertEquals("text/plain", invoker.contentType());
            invoker.renderType("json");
            invoker.contentType("application/custom+json");
            invoker.getHttpResponse().setContentType(invoker.contentType());
            return Map.of("value", "body");
        }
    }

    @Test
    public void controllerChoicesOverrideAnnotationDefaultsDuringNormalRendering() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(OverrideAction.class);
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                fail("The annotation must not overwrite the controller's renderer choice");
            });
            binder.addRender("json").toProvider(() -> (invoker, writer) -> {
                assertEquals("application/custom+json", invoker.contentType());
                assertEquals(Map.of("value", "body"), invoker.get(Invoker.RETURN_DATA_KEY));
                writer.write("overridden");
            });
        })) {
            assertEquals("overridden", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }
}
