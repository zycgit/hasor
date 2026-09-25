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
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.function.EFunction;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Any;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;
import net.hasor.web.render.RenderType;
import net.hasor.web.wrap.InvokerWrap;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResponseHandlingTest extends AbstractTest {
    public static class ResponseAction {
        private final EFunction<HttpServletResponse, Object, Throwable> action;

        public ResponseAction(EFunction<HttpServletResponse, Object, Throwable> action) {
            this.action = action;
        }

        @Any
        @RenderType("text")
        public Object execute(HttpServletResponse response) throws Throwable {
            return this.action.eApply(response);
        }
    }

    public static class VoidResponseAction {
        @Any
        @RenderType("text")
        public void execute(ServletResponse response) {
            response.setCharacterEncoding("UTF-8");
        }
    }

    public static class RenderAction {
        @Any
        @RenderType("text")
        public String execute(RenderInvoker invoker) {
            if (invoker.getHttpRequest().getParameter("handled") != null) {
                invoker.setSkipRender();
            }
            if (invoker.getHttpRequest().getParameter("view") != null) {
                invoker.renderTo("page.html");
                if (invoker.getHttpRequest().getParameter("layout") == null) {
                    invoker.layoutDisable();
                }
            }
            return "body";
        }
    }

    public static class SkippedRenderAction {
        @Any
        @RenderType(engineType = UnexpectedRenderEngine.class)
        public String execute(Invoker invoker) {
            invoker.setSkipRender();
            return "body";
        }
    }

    public static class UnexpectedRenderEngine implements RenderEngine {
        @Override
        public void process(RenderInvoker invoker, Writer writer) {
            throw new AssertionError("Skipped rendering must not invoke the engine");
        }
    }

    @Test
    public void markingDuringPreHandleSkipsOnlyRenderingAndSharesStateWithWrappers() throws Throwable {
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(RenderAction.class);
            binder.filter("/*").through((invoker, chain) -> {
                Object result = chain.doNext(invoker);
                events.add(result.toString());
                return result;
            });
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(Invoker invoker) {
                    assertFalse(invoker.isSkipRender());
                    if (invoker.getHttpRequest().getParameter("skip") != null) {
                        InvokerWrap wrapper = new InvokerWrap(invoker);
                        wrapper.setSkipRender();
                        wrapper.setSkipRender();
                        assertTrue(wrapper.isSkipRender());
                        assertTrue(invoker.isSkipRender());
                    }
                    events.add("pre");
                    return true;
                }

                @Override
                public void postHandle(Invoker invoker, Object result) {
                    assertEquals("body", result);
                    boolean marked = invoker.getHttpRequest().getParameter("skip") != null;
                    assertEquals(marked, invoker.isSkipRender());
                    events.add("post");
                }

                @Override
                public void afterCompletion(Invoker invoker, Throwable failure) {
                    assertNull(failure);
                    events.add("complete");
                }
            });
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test?skip=true", app));
            assertEquals(List.of("pre", "post", "complete", "body"), events);
            events.clear();
            assertEquals("body", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(List.of("pre", "post", "complete", "body"), events);
        }
    }

    @Test
    public void postHandleCanSuppressExplicitRenderingAndTemplateRendering() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(RenderAction.class);
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public void postHandle(Invoker invoker, Object result) {
                    assertEquals("body", result);
                    invoker.setSkipRender();
                }
            });
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                throw new AssertionError("The completed response must not be rendered");
            });
        })) {
            for (String query : List.of("", "?view=true", "?view=true&layout=true")) {
                assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test" + query, app));
            }
        }
    }

    @Test
    public void controllerSkipsItsRenderEngineOutput() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(SkippedRenderAction.class);
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    @Test
    public void voidAndNullResponseMethodsSkipExplicitRenderingWithoutAffectingLaterRequests() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/void").with(VoidResponseAction.class);
            binder.mappingTo("/null").with(new ResponseAction(response -> {
                response.setStatus(202);
                return null;
            }));
            binder.mappingTo("/value").with(new ResponseAction(response -> "value"));
            binder.addRender("text").toProvider(() -> (invoker, writer) -> writer.write("rendered"));
        })) {
            for (String path : List.of("/void", "/null")) {
                assertEquals("", this.mockAndCallHttp("GET", "http://localhost" + path, app));
                assertEquals("rendered", this.mockAndCallHttp("GET", "http://localhost/value", app));
            }
        }
    }

    @Test
    public void settingResponseHeadersDoesNotSuppressANonNullReturnValue() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new ResponseAction(response -> {
                response.setHeader("X-Controller", "true");
                return "body";
            }));
        })) {
            assertEquals("body", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    @Test
    public void responseParameterDoesNotPreventResolvingAControllerFailure() throws Throwable {
        IOException failure = new IOException("controller failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new ResponseAction(response -> {
                response.setHeader("X-Controller", "true");
                throw failure;
            }));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                assertSame(failure, error);
                return "recovered";
            });
        })) {
            assertEquals("recovered", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }
}
