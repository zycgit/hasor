/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.function.EFunction;
import net.hasor.cobble.io.output.WriterOutputStream;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.*;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.Async;
import net.hasor.web.binder.OneConfig;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AsyncInterceptorTest extends AbstractTest {
    public static class AsyncAction extends HandlerInterceptorTest.Action {
        public AsyncAction(EFunction<Invoker, Object, Throwable> action) {
            super(action);
        }

        @Override
        @Any
        @Async
        public Object execute(Invoker invoker) throws Throwable {
            return super.execute(invoker);
        }
    }

    @Test
    public void frameworkAsyncRendersResultsAndRunsMvcWithRequestParametersOnTheWorker() throws Throwable {
        for (boolean fail : List.of(false, true)) {
            List<String> events = new ArrayList<>();
            AtomicReference<Thread> worker = new AtomicReference<>();
            List<Throwable> completions = new ArrayList<>();
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test/{id}").with(new AsyncAction(invoker -> {
                    events.add("action");
                    assertSame(worker.get(), Thread.currentThread());
                    assertEquals("123", HttpParameters.pathMap().get("id"));
                    assertEquals("value", HttpParameters.queryMap().get("name"));
                    if (fail) {
                        throw new IOException("controller");
                    }
                    return "ok";
                }));
                binder.filter("/*").through((invoker, chain) -> {
                    worker.set(Thread.currentThread());
                    events.add("filter.before");
                    Object result = chain.doNext(invoker);
                    events.add("filter.after");
                    return result;
                });
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(Invoker invoker) {
                        assertSame(worker.get(), Thread.currentThread());
                        assertSame(invoker, HttpParameters.localInvoker());
                        events.add("pre");
                        return true;
                    }

                    @Override
                    public void postHandle(Invoker invoker, Object result) {
                        assertSame(worker.get(), Thread.currentThread());
                        events.add("post");
                    }

                    @Override
                    public void afterCompletion(Invoker invoker, Throwable failure) {
                        completions.add(failure);
                        events.add("complete");
                    }
                });
                binder.addExceptionHandler(IOException.class, (invoker, error) -> {
                    assertSame(worker.get(), Thread.currentThread());
                    assertEquals("value", HttpParameters.requestMap().get("name"));
                    events.add("handler");
                    return "recovered";
                });
            })) {
                HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/test/123?name=value"));
                AsyncContext async = this.trackAsyncState(request);
                AtomicBoolean cleaned = new AtomicBoolean();
                doAnswer(call -> {
                    cleaned.set(HttpParameters.localInvoker() == null && HttpParameters.requestArrayMap() == null);
                    return null;
                }).when(async).complete();
                HttpServletResponse response = mock(HttpServletResponse.class);
                this.mockRenderResponse(response);
                StringWriter body = new StringWriter();
                when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new WriterOutputStream(body)));
                InvokerContext context = new InvokerContext();
                context.initContext(app, new OneConfig("test", () -> app));
                try {
                    assertEquals(fail ? "recovered" : "ok", context.genCaller(request, response).invoke(null).get());
                    assertEquals(fail ? "recovered" : "ok", body.toString());
                    assertNotSame(Thread.currentThread(), worker.get());
                    assertEquals(List.of("filter.before", "pre", "action", fail ? "handler" : "post", "complete", "filter.after"), events);
                    assertEquals(1, completions.size());
                    assertNull(completions.get(0));
                    assertTrue(cleaned.get());
                    verify(async).complete();
                } finally {
                    context.destroyContext();
                }
            }
        }
    }

    private AsyncContext trackAsyncState(HttpServletRequest request) {
        AsyncContext async = request.getAsyncContext();
        AtomicBoolean started = new AtomicBoolean();
        when(request.isAsyncStarted()).thenAnswer(call -> started.get());
        when(request.startAsync()).thenAnswer(call -> {
            started.set(true);
            return async;
        });
        when(request.startAsync(any(), any())).thenAnswer(call -> {
            started.set(true);
            return async;
        });
        return async;
    }

    @Test
    public void nativeAsyncDefersCompletionAndDoesNotRenderOrCompleteTwice() throws Throwable {
        for (String outcome : List.of("complete", "error", "timeout")) {
            List<String> events = new ArrayList<>();
            List<Throwable> failures = new ArrayList<>();
            try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
                binder.mappingTo("/test").with(new HandlerInterceptorTest.Action(invoker -> {
                    invoker.getHttpRequest().startAsync();
                    return "owned by async code";
                }));
                binder.bindInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(Invoker invoker) {
                        events.add("pre");
                        return true;
                    }

                    @Override
                    public void postHandle(Invoker invoker, Object result) {
                        events.add("post");
                    }

                    @Override
                    public void afterCompletion(Invoker invoker, Throwable failure) {
                        events.add("complete");
                        failures.add(failure);
                    }
                });
            })) {
                HttpServletRequest request = this.mockRequest("GET", new URL("http://localhost/test"));
                AsyncContext async = this.trackAsyncState(request);
                AtomicReference<AsyncListener> listener = new AtomicReference<>();
                doAnswer(call -> {
                    listener.set(call.getArgument(0));
                    return null;
                }).when(async).addListener(any(AsyncListener.class));
                HttpServletResponse response = mock(HttpServletResponse.class);
                InvokerContext context = new InvokerContext();
                context.initContext(app, new OneConfig("test", () -> app));
                try {
                    context.genCaller(request, response).invoke(null).get();
                    assertEquals(List.of("pre"), events);
                    verify(response, never()).getWriter();
                    verify(response, never()).getOutputStream();
                    verify(async, never()).complete();
                    assertNotNull(listener.get());
                    IOException failure = new IOException("async error");
                    AsyncEvent event = new AsyncEvent(async, failure);
                    if (outcome.equals("error")) {
                        listener.get().onError(event);
                    } else if (outcome.equals("timeout")) {
                        listener.get().onTimeout(event);
                    }
                    listener.get().onComplete(event);
                    listener.get().onComplete(event);
                    assertEquals(List.of("pre", "complete"), events);
                    assertEquals(1, failures.size());
                    if (outcome.equals("error")) {
                        assertSame(failure, failures.get(0));
                    } else if (outcome.equals("timeout")) {
                        assertTrue(failures.get(0) instanceof TimeoutException);
                    } else {
                        assertNull(failures.get(0));
                    }
                } finally {
                    context.destroyContext();
                }
            }
        }
    }
}
