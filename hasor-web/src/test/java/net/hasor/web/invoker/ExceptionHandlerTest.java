/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.Async;
import net.hasor.web.binder.OneConfig;
import net.hasor.web.wrap.WebApiBinderWrap;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExceptionHandlerTest extends AbstractTest {
    public static class FailedAction {
        private final Throwable failure;

        public FailedAction(Throwable failure) {
            this.failure = failure;
        }

        @Any
        public void execute(Invoker invoker) throws Throwable {
            if (invoker.getHttpRequest().getParameter("skip") != null) {
                invoker.setSkipRender();
            }
            throw this.failure;
        }
    }

    public static class AsyncFailedAction extends FailedAction {
        public AsyncFailedAction(Throwable failure) {
            super(failure);
        }

        @Override
        @Any
        @Async
        public void execute(Invoker invoker) throws Throwable {
            super.execute(invoker);
        }
    }

    public static class FailedValueAction {
        private final Throwable failure;

        public FailedValueAction(Throwable failure) {
            this.failure = failure;
        }

        @Any
        public Object execute(Invoker invoker) throws Throwable {
            if (invoker.getHttpRequest().getParameter("skip") != null) {
                invoker.setSkipRender();
            }
            throw this.failure;
        }
    }

    public static class SuccessfulAction {
        @Any
        public String execute() {
            return "ok";
        }
    }

    public static class SkippedAction {
        @Any
        public String execute(Invoker invoker) {
            invoker.setSkipRender();
            return "skipped";
        }
    }

    @Test
    public void closestExceptionTypeWinsRegardlessOfRegistrationOrder() throws Throwable {
        FileNotFoundException failure = new FileNotFoundException("controller failure");
        Map<String, String> result = Map.of("message", "handled");
        for (boolean parentFirst : List.of(false, true)) {
            AtomicInteger calls = new AtomicInteger();
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new FailedAction(failure));
                if (parentFirst) {
                    binder.addExceptionHandler(Exception.class, (invoker, error) -> "parent");
                }
                binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                    assertSame(failure, error);
                    calls.incrementAndGet();
                    return result;
                });
                if (!parentFirst) {
                    binder.addExceptionHandler(Exception.class, (invoker, error) -> "parent");
                }
                binder.filter("/*").through((invoker, chain) -> {
                    Object value = chain.doNext(invoker);
                    assertSame(result, value);
                    assertSame(result, invoker.get(Invoker.RETURN_DATA_KEY));
                    return value;
                });
            })) {
                assertEquals("{\"message\":\"handled\"}", this.mockAndCallHttp("GET", "http://localhost/test", app));
                assertEquals(1, calls.get());
            }
        }
    }

    @Test
    public void handledValuesAreReturnedByVoidNonVoidAndAsyncInvocations() throws Throwable {
        IOException failure = new IOException("controller failure");
        for (Object action : List.of(new FailedAction(failure), new FailedValueAction(failure), new AsyncFailedAction(failure))) {
            for (Object value : List.of(Map.of("message", "handled"), false, 0, "")) {
                AtomicInteger calls = new AtomicInteger();
                try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                    binder.mappingTo("/test").with(action);
                    binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                        assertSame(failure, error);
                        calls.incrementAndGet();
                        return value;
                    });
                })) {
                    HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/test"));
                    HttpServletResponse response = mock(HttpServletResponse.class);
                    this.mockRenderResponse(response);
                    InvokerContext context = new InvokerContext();
                    context.initContext(app, new OneConfig("test", () -> app));
                    try {
                        assertSame(value, context.genCaller(request, response).invoke(null).get());
                        assertSame(value, request.getAttribute(Invoker.RETURN_DATA_KEY));
                        assertEquals(1, calls.get());
                    } finally {
                        context.destroyContext();
                    }
                }
            }
        }
    }

    @Test
    public void aSkipFlagFromTheOuterFilterDoesNotBypassExceptionHandling() throws Throwable {
        IOException failure = new IOException("controller failure");
        AtomicInteger calls = new AtomicInteger();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(failure));
            binder.filter("/*").through((invoker, chain) -> {
                invoker.setSkipRender();
                Object result = chain.doNext(invoker);
                assertEquals("handled", result);
                assertEquals(result, invoker.get(Invoker.RETURN_DATA_KEY));
                return result;
            });
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                assertSame(failure, error);
                assertTrue(invoker.isSkipRender());
                calls.incrementAndGet();
                return "handled";
            });
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(1, calls.get());
        }
    }

    @Test
    public void skipRenderControlFlowDoesNotEnterBusinessExceptionHandlers() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(SkippedAction.class);
            binder.addExceptionHandler(Throwable.class, (invoker, error) -> {
                fail("Skipping rendering is not a business failure");
                return null;
            });
            binder.filter("/*").through((invoker, chain) -> {
                Object result = chain.doNext(invoker);
                assertEquals("skipped", result);
                return result;
            });
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    @Test
    public void handlerFailuresDoNotEnterAnotherExceptionHandler() throws Throwable {
        IOException original = new IOException("controller failure");
        IllegalStateException failure = new IllegalStateException("handler failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(original));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                throw failure;
            });
            binder.addExceptionHandler(IllegalStateException.class, (invoker, error) -> {
                fail("An exception handler failure must propagate to the outer caller");
                return "recursive";
            });
        })) {
            this.assertFailure(app, failure);
            assertArrayEquals(new Throwable[] { original }, failure.getSuppressed());
        }
    }

    @Test
    public void closestRegisteredTypeWinsAndRendersDataForVoidAction() throws Throwable {
        FileNotFoundException failure = new FileNotFoundException("controller failure");
        for (boolean parentFirst : List.of(false, true)) {
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new FailedAction(failure));
                if (parentFirst) {
                    binder.addExceptionHandler(Exception.class, (invoker, exception) -> "parent");
                }
                binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                    assertSame(failure, exception);
                    invoker.getHttpResponse().setStatus(409);
                    return Map.of("message", "host response");
                });
                if (!parentFirst) {
                    binder.addExceptionHandler(Exception.class, (invoker, exception) -> "parent");
                }
            })) {
                Set<String> contentTypes = new HashSet<>();
                assertEquals("{\"message\":\"host response\"}", this.mockAndCallHttp("GET", "http://localhost/test", app, contentTypes, null));
                assertTrue(contentTypes.stream().anyMatch(type -> type.startsWith("application/json")));
            }
        }
    }

    @Test
    public void unhandledFailuresKeepTheOriginalException() throws Throwable {
        IOException failure = new IOException("unhandled");
        for (Object action : List.of(new FailedAction(failure), new FailedValueAction(failure), new AsyncFailedAction(failure))) {
            for (boolean register : List.of(false, true)) {
                try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                    binder.mappingTo("/test").with(action);
                    binder.addExceptionHandler(IllegalArgumentException.class, (invoker, exception) -> "unrelated");
                    if (register) {
                        binder.addExceptionHandler(IOException.class, (invoker, exception) -> null);
                        binder.addExceptionHandler(Throwable.class, (invoker, exception) -> {
                            fail("A null result must propagate, not fall back to a broader handler");
                            return "parent";
                        });
                    }
                })) {
                    for (String suffix : List.of("", "?skip=true")) {
                        try {
                            this.mockAndCallHttp("GET", "http://localhost/test" + suffix, app);
                            fail("An unresolved exception must propagate regardless of SkipRender");
                        } catch (ExecutionException expected) {
                            assertSame(failure, expected.getCause());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void skippingRenderingDoesNotSuppressOrBypassExceptionHandling() throws Throwable {
        IOException failure = new IOException("unhandled");
        for (boolean async : List.of(false, true)) {
            for (boolean recover : List.of(false, true)) {
                AtomicInteger calls = new AtomicInteger();
                try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                    FailedAction action = async ? new AsyncFailedAction(failure) : new FailedAction(failure);
                    binder.mappingTo("/test").with(action);
                    binder.bindInterceptor(new HandlerInterceptor() {
                        @Override
                        public boolean preHandle(Invoker invoker) {
                            invoker.setSkipRender();
                            return true;
                        }
                    });
                    binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                        assertSame(failure, exception);
                        calls.incrementAndGet();
                        return recover ? "recovered" : null;
                    });
                })) {
                    if (recover) {
                        assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
                    } else {
                        this.assertFailure(app, failure);
                    }
                    assertEquals(1, calls.get());
                }
            }
        }
    }

    @Test
    public void nonNullValuesIncludingFalseAndEmptyTextAreResponses() throws Throwable {
        IOException failure = new IOException("controller failure");
        for (Object action : List.of(new FailedAction(failure), new FailedValueAction(failure))) {
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(action);
                new WebApiBinderWrap(binder).addExceptionHandler(IOException.class, (invoker, exception) -> {
                    return switch (invoker.getHttpRequest().getParameter("value")) {
                        case "false" -> false;
                        case "0" -> 0;
                        case "text" -> "handled";
                        default -> "";
                    };
                });
            })) {
                for (String value : List.of("false", "0", "empty", "text")) {
                    String expected = value.equals("empty") ? "" : value.equals("text") ? "handled" : value;
                    assertEquals(expected, this.mockAndCallHttp("GET", "http://localhost/test?value=" + value, app));
                }
            }
        }
    }

    @Test
    public void invocationAndExceptionHandlingFinishBeforeRenderingIsSelected() throws Throwable {
        IOException failure = new IOException("controller failure");
        for (Object action : List.of(new FailedAction(failure), new FailedValueAction(failure))) {
            List<String> events = new ArrayList<>();
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(action);
                binder.filter("/*").through((invoker, chain) -> {
                    events.add("filter.before");
                    if (invoker.getHttpRequest().getParameter("filter") != null) {
                        invoker.setSkipRender();
                    }
                    Object result = chain.doNext(invoker);
                    assertEquals("handled", result);
                    assertEquals(result, invoker.get(Invoker.RETURN_DATA_KEY));
                    events.add("filter.after");
                    return result;
                });
                binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                    assertSame(failure, error);
                    events.add("handler");
                    return "handled";
                });
                binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                    events.add("render");
                    writer.write(invoker.get(Invoker.RETURN_DATA_KEY).toString());
                });
            })) {
                assertEquals("handled", this.mockAndCallHttp("GET", "http://localhost/test", app));
                assertEquals(List.of("filter.before", "handler", "render", "filter.after"), events);
                for (String source : List.of("filter", "skip")) {
                    events.clear();
                    assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test?" + source + "=true", app));
                    assertEquals(List.of("filter.before", "handler", "filter.after"), events);
                }
            }
        }
    }

    @Test
    public void duplicateTypesAreRejectedDuringRegistration() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> "first");
            try {
                binder.addExceptionHandler(IOException.class, (invoker, exception) -> "second");
                fail("Duplicate registrations must not silently replace a handler");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains(IOException.class.getName()));
            }
        })) {
            assertNotNull(app);
        }
    }

    @Test
    public void headResponseRendersHeadersWithoutBody() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(new IOException()));
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> "head error");
        })) {
            Set<String> contentTypes = new HashSet<>();
            assertEquals("", this.mockAndCallHttp("HEAD", "http://localhost/test", app, contentTypes, null));
            assertTrue(contentTypes.stream().anyMatch(type -> type.startsWith("text/plain")));
        }
    }

    @Test
    public void noContentStatusesSkipTheErrorBody() throws Throwable {
        for (int status : List.of(204, 304)) {
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new FailedAction(new IOException()));
                binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                    invoker.getHttpResponse().setStatus(status);
                    return "must not be written";
                });
            })) {
                HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/test"));
                HttpServletResponse response = mock(HttpServletResponse.class);
                when(response.getStatus()).thenReturn(status);
                InvokerContext context = new InvokerContext();
                context.initContext(app, new OneConfig("test", () -> app));
                try {
                    assertEquals("must not be written", context.genCaller(request, response).invoke(null).get());
                    verify(response, never()).getWriter();
                    verify(response, never()).getOutputStream();
                } finally {
                    context.destroyContext();
                }
            }
        }
    }

    @Test
    public void handlerCanOwnTheResponseWithoutAppendedRendering() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(new IOException()));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                invoker.getHttpResponse().getWriter().write("custom body");
                return Map.of("message", "must not be appended");
            });
        })) {
            assertEquals("custom body", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    @Test
    public void nullResultStillPropagatesAfterTheHandlerWritesToTheResponse() throws Throwable {
        IOException failure = new IOException("unhandled");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(failure));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                invoker.getHttpResponse().getWriter().write("partial response");
                invoker.setSkipRender();
                return null;
            });
        })) {
            this.assertFailure(app, failure);
        }
    }

    @Test
    public void handledExceptionResponsesSkipDefaultRendering() throws Throwable {
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(new IOException()));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                invoker.getHttpResponse().setStatus(401);
                invoker.setSkipRender();
                return "must not be rendered";
            });
        })) {
            assertEquals("", this.mockAndCallHttp("GET", "http://localhost/test", app));
        }
    }

    private void assertFailure(AppContext app, Throwable failure) throws Throwable {
        try {
            this.mockAndCallHttp("GET", "http://localhost/test", app);
            fail("Expected an unresolved MVC failure");
        } catch (ExecutionException expected) {
            assertSame(failure, expected.getCause());
        }
    }

    @Test
    public void asyncRequestsUseTheSameHandlers() throws Throwable {
        IOException failure = new IOException("async failure");
        AtomicInteger calls = new AtomicInteger();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new AsyncFailedAction(failure));
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                assertSame(failure, exception);
                calls.incrementAndGet();
                return "async host response";
            });
        })) {
            assertEquals("async host response", this.mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(1, calls.get());
        }
    }

    @Test
    public void filterFailuresStayOutsideMvcExceptionHandling() throws Throwable {
        IOException failure = new IOException("filter failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(SuccessfulAction.class);
            binder.filter("/*").through((invoker, chain) -> {
                throw failure;
            });
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                fail("HTTP filter failures must propagate to the outer host");
                return null;
            });
        })) {
            this.assertFailure(app, failure);
        }
    }

    @Test
    public void renderFailuresDoNotReenterMvcExceptionHandling() throws Throwable {
        IOException failure = new IOException("render failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(SuccessfulAction.class);
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                throw failure;
            });
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                fail("Rendering runs after MVC exception handling");
                return null;
            });
        })) {
            this.assertFailure(app, failure);
        }
    }

    @Test
    public void renderingAHandledResultKeepsTheOriginalFailureWithoutRetrying() throws Throwable {
        IOException original = new IOException("controller failure");
        IllegalStateException failure = new IllegalStateException("render failure");
        AtomicInteger calls = new AtomicInteger();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(original));
            binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                calls.incrementAndGet();
                return "handled";
            });
            binder.addExceptionHandler(IllegalStateException.class, (invoker, error) -> {
                fail("A render failure must not restart exception handling");
                return null;
            });
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                throw failure;
            });
        })) {
            this.assertFailure(app, failure);
            assertEquals(1, calls.get());
            assertArrayEquals(new Throwable[] { original }, failure.getSuppressed());
        }
    }

    @Test
    public void handlerFailurePreservesTheOriginalAsSuppressed() throws Throwable {
        IOException original = new IOException("controller failure");
        IllegalStateException failure = new IllegalStateException("handler failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(original));
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                throw failure;
            });
        })) {
            this.assertFailure(app, failure);
            assertArrayEquals(new Throwable[] { original }, failure.getSuppressed());
        }
    }

    @Test
    public void committedResponsesBypassHandlers() throws Throwable {
        IOException failure = new IOException("committed failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(failure));
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                fail("Committed responses belong to the host");
                return null;
            });
        })) {
            HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/test"));
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(response.isCommitted()).thenReturn(true);
            InvokerContext context = new InvokerContext();
            context.initContext(app, new OneConfig("test", () -> app));
            try {
                context.genCaller(request, response).invoke(null).get();
                fail("Expected original failure");
            } catch (ExecutionException expected) {
                assertSame(failure, expected.getCause());
            } finally {
                context.destroyContext();
            }
        }
    }

    @Test
    public void partiallyWrittenResponsesAreNotOverwritten() throws Throwable {
        IOException failure = new IOException("partial failure");
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(new FailedAction(failure));
            binder.filter("/*").through((invoker, chain) -> {
                invoker.getHttpResponse().getWriter().write("partial");
                return chain.doNext(invoker);
            });
            binder.addExceptionHandler(IOException.class, (invoker, exception) -> {
                fail("Started responses must not be overwritten");
                return null;
            });
        })) {
            this.assertFailure(app, failure);
        }
    }
}
