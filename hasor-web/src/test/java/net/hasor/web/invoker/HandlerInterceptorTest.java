/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.function.EFunction;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Any;
import net.hasor.web.binder.OneConfig;
import net.hasor.web.wrap.WebApiBinderWrap;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class HandlerInterceptorTest extends AbstractTest {
    public static class Action {
        private final EFunction<Invoker, Object, Throwable> action;

        public Action(EFunction<Invoker, Object, Throwable> action) {
            this.action = action;
        }

        @Any
        public Object execute(Invoker invoker) throws Throwable {
            return this.action.eApply(invoker);
        }
    }

    public static class VoidAction {
        @Any
        public void execute() {
        }
    }

    private record RecordingInterceptor(String name, List<String> events) implements HandlerInterceptor {

        @Override
        public boolean preHandle(Invoker invoker) {
            this.events.add(this.name + ".pre");
            return true;
        }

        @Override
        public void postHandle(Invoker invoker, Object result) {
            this.events.add(this.name + ".post");
        }

        @Override
        public void afterCompletion(Invoker invoker, Throwable failure) {
            assertNull(failure);
            this.events.add(this.name + ".complete");
        }
    }

    @Test
    public void registrationOrderControlsCallbacksAndFiltersSurroundTheEntireMvcLifecycle() throws Throwable {
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new Action(invoker -> {
                events.add("action");
                return "ok";
            }));
            binder.filter("/*").through((invoker, chain) -> {
                events.add("filter.before");
                Object result = chain.doNext(invoker);
                events.add("filter.after");
                return result;
            });
            binder.bindInterceptor(new RecordingInterceptor("first", events));
            WebApiBinderWrap wrapped = new WebApiBinderWrap(binder);
            assertSame(wrapped, wrapped.bindInterceptor(new RecordingInterceptor("a", events)));
            binder.bindInterceptor(new RecordingInterceptor("b", events));
            wrapped.bindInterceptor(new RecordingInterceptor("last", events));
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                events.add("render");
                writer.write(invoker.get(Invoker.RETURN_DATA_KEY).toString());
            });
        })) {
            assertEquals("ok", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(List.of("filter.before", "first.pre", "a.pre", "b.pre", "last.pre", "action", "last.post", "b.post", "a.post", "first.post", "render", "last.complete", "b.complete", "a.complete", "first.complete", "filter.after"), events);
        }
    }

    @Test
    public void postHandleCanReplaceTheResultBeforeRendering() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new Action(invoker -> "controller"));
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public void postHandle(Invoker invoker, Object result) {
                    assertEquals("controller", result);
                    invoker.put(Invoker.RETURN_DATA_KEY, Map.of("value", result));
                }
            });
        })) {
            assertEquals("{\"value\":\"controller\"}", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    @Test
    public void finalResultRenderingIsIndependentOfTheControllerReturnType() throws Throwable {
        for (Object action : List.of(new VoidAction(), new Action(invoker -> null))) {
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(action);
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public void postHandle(Invoker invoker, Object result) {
                        assertNull(result);
                        if (invoker.getHttpRequest().getParameter("replace") != null) {
                            invoker.put(Invoker.RETURN_DATA_KEY, Map.of("value", "final result"));
                        }
                    }
                });
            })) {
                assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
                assertEquals("{\"value\":\"final result\"}", this.mockAndCallHttp("GET", "http://localhost/test?replace=true", app));
            }
        }
    }

    @Test
    public void shortCircuitCompletesOnlyEnteredInterceptorsAndDoesNotLeakToTheNextRequest() throws Throwable {
        AtomicBoolean blocked = new AtomicBoolean(true);
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new Action(invoker -> {
                events.add("action");
                return "ok";
            }));
            binder.bindInterceptor(new RecordingInterceptor("first", events));
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(Invoker invoker) {
                    events.add("gate.pre");
                    if (blocked.getAndSet(false)) {
                        invoker.getHttpResponse().setStatus(401);
                        return false;
                    }
                    return true;
                }

                @Override
                public void afterCompletion(Invoker invoker, Throwable failure) {
                    assertNull(failure);
                    events.add("gate.complete");
                }
            });
            binder.bindInterceptor(new RecordingInterceptor("last", events));
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(List.of("first.pre", "gate.pre", "first.complete"), events);
            events.clear();
            assertEquals("ok", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(List.of("first.pre", "gate.pre", "last.pre", "action", "last.post", "first.post", "last.complete", "gate.complete", "first.complete"), events);
        }
    }

    @Test
    public void mvcFailuresAreResolvedAtTheSameBoundaryAndCompletionSeesTheOutcome() throws Throwable {
        for (String phase : List.of("pre", "action", "post")) {
            IOException failure = new IOException(phase);
            List<String> events = new ArrayList<>();
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new Action(invoker -> {
                    events.add("action");
                    if (phase.equals("action")) {
                        throw failure;
                    }
                    return "ok";
                }));
                binder.bindInterceptor(new RecordingInterceptor("first", events));
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(Invoker invoker) throws Throwable {
                        events.add("second.pre");
                        if (phase.equals("pre")) {
                            throw failure;
                        }
                        return true;
                    }

                    @Override
                    public void postHandle(Invoker invoker, Object result) throws Throwable {
                        events.add("second.post");
                        throw failure;
                    }

                    @Override
                    public void afterCompletion(Invoker invoker, Throwable error) {
                        assertNull(error);
                        events.add("second.complete");
                    }
                });
                binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                    assertSame(failure, error);
                    events.add("handler");
                    return "recovered";
                });
            })) {
                assertEquals("recovered", this.mockAndCallHttp("GET", "http://localhost/test", app));
                List<String> expected = new ArrayList<>(List.of("first.pre", "second.pre"));
                if (!phase.equals("pre")) {
                    expected.add("action");
                }
                if (phase.equals("post")) {
                    expected.add("second.post");
                }
                expected.add("handler");
                if (!phase.equals("pre")) {
                    expected.add("second.complete");
                }
                expected.add("first.complete");
                assertEquals(phase, expected, events);
            }
        }
    }

    @Test
    public void cleanupFailuresDoNotHideTheOriginalFailureOrSkipOtherCompletions() throws Throwable {
        IOException original = new IOException("controller");
        List<String> events = new ArrayList<>();
        List<Throwable> completionFailures = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new Action(invoker -> {
                throw original;
            }));
            for (int index = 1; index <= 2; index++) {
                String name = "interceptor" + index;
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public void afterCompletion(Invoker invoker, Throwable failure) {
                        events.add(name);
                        completionFailures.add(failure);
                        throw new IllegalStateException("cleanup");
                    }
                });
            }
            binder.addExceptionHandler(IllegalStateException.class, (invoker, failure) -> {
                fail("Completion failures must not enter MVC exception handling");
                return null;
            });
        })) {
            try {
                this.mockAndCallHttp("GET", "http://localhost/test", app);
                fail("The original failure must propagate");
            } catch (ExecutionException expected) {
                assertSame(original, expected.getCause());
            }
            assertEquals(List.of("interceptor2", "interceptor1"), events);
            assertEquals(List.of(original, original), completionFailures);
        }
    }

    @Test
    public void handlerCanWriteItsOwnResponseAndCompleteNormally() throws Throwable {
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new Action(invoker -> {
                throw new IOException();
            }));
            binder.bindInterceptor(new RecordingInterceptor("interceptor", events));
            binder.addExceptionHandler(IOException.class, (invoker, failure) -> {
                invoker.getHttpResponse().getWriter().write("host body");
                return "";
            });
        })) {
            assertEquals("host body", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(List.of("interceptor.pre", "interceptor.complete"), events);
        }
    }

    @Test
    public void postHandleCanWriteTheResponseOrRemoveTheDefaultBody() throws Throwable {
        for (boolean writeBody : List.of(false, true)) {
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new Action(invoker -> "must not render"));
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public void postHandle(Invoker invoker, Object result) throws Throwable {
                        if (writeBody) {
                            invoker.getHttpResponse().getWriter().write("owned");
                        } else {
                            invoker.put(Invoker.RETURN_DATA_KEY, null);
                        }
                    }
                });
            })) {
                assertEquals(writeBody ? "owned" : "", this.mockAndCallHttp("GET", "http://localhost/test", app));
            }
        }
    }

    @Test
    public void outerFiltersCanRecoverMvcFailuresAndTheirOwnFailuresNeverEnterMvc() throws Throwable {
        for (boolean filterFails : List.of(false, true)) {
            IOException failure = new IOException("unresolved");
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new Action(invoker -> {
                    if (!filterFails) {
                        throw failure;
                    }
                    return null;
                }));
                binder.filter("/*").through((invoker, chain) -> {
                    try {
                        chain.doNext(invoker);
                    } catch (IOException original) {
                        assertSame(failure, original);
                        invoker.getHttpResponse().getWriter().write("filter response");
                    }
                    if (filterFails) {
                        throw failure;
                    }
                    return null;
                });
                if (filterFails) {
                    binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                        fail("Outer filter failures cannot reenter MVC exception handling");
                        return null;
                    });
                }
            })) {
                if (filterFails) {
                    try {
                        this.mockAndCallHttp("GET", "http://localhost/test", app);
                        fail("Expected a filter failure");
                    } catch (ExecutionException expected) {
                        assertSame(failure, expected.getCause());
                    }
                } else {
                    assertEquals("filter response", this.mockAndCallHttp("GET", "http://localhost/test", app));
                }
            }
        }
    }

    @Test
    public void unmatchedRequestsUseFiltersAndDownstreamFailuresDoNotEnterMvc() throws Throwable {
        IOException failure = new IOException("downstream servlet");
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.bindInterceptor(new RecordingInterceptor("mvc", events));
            binder.filter("/*").through((invoker, chain) -> {
                assertNull(invoker.ownerMapping());
                events.add("filter");
                return chain.doNext(invoker);
            });
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                fail("Unmatched requests have no MVC exception scope");
                return null;
            });
        })) {
            InvokerContext context = new InvokerContext();
            context.initContext(app, new OneConfig("test", () -> app));
            HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/other"));
            HttpServletResponse response = mock(HttpServletResponse.class);
            try {
                context.genCaller(request, response).invoke((req, res) -> {
                    throw failure;
                }).get();
                fail("Expected the downstream failure");
            } catch (ExecutionException expected) {
                assertSame(failure, expected.getCause());
            } finally {
                context.destroyContext();
            }
            assertEquals(List.of("filter"), events);
        }
    }
}
