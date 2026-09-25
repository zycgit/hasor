/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import net.hasor.core.Hasor;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.Async;
import net.hasor.web.render.RenderType;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises MVC boundaries through a real HTTP connection in each supported container. */
public abstract class MvcInterceptionIntegrationTest extends ContainerIntegrationTest {
    private       WebServer              server;
    private final List<String>           events            = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Boolean> completedRequests = new LinkedBlockingQueue<>();

    public static class Action {
        @Any
        public String execute(Invoker invoker) throws IOException {
            if (invoker.getHttpRequest().getParameter("fail") != null) {
                throw new IOException("controller failure");
            }
            if (invoker.getHttpRequest().getParameter("handled") != null) {
                invoker.setSkipRender();
            }
            return "ok";
        }
    }

    public static class AsyncAction extends Action {
        @Override
        @Any
        @Async
        public String execute(Invoker invoker) throws IOException {
            return super.execute(invoker);
        }
    }

    public static class VoidAction {
        @Any
        public void execute() throws IOException {
            throw new IOException("void controller failure");
        }
    }

    public static class AsyncVoidAction extends VoidAction {
        @Override
        @Any
        @Async
        public void execute() throws IOException {
            super.execute();
        }
    }

    public static class DirectResponseAction {
        @Any
        @RenderType("manual")
        public void execute(HttpServletResponse response) {
            response.setStatus(202);
        }
    }

    public static class AsyncDirectResponseAction extends DirectResponseAction {
        @Override
        @Any
        @Async
        @RenderType("manual")
        public void execute(HttpServletResponse response) {
            super.execute(response);
        }
    }

    private void start(boolean recover) throws Exception {
        WebServerConfig config = new WebServerConfig().port(0).contextPath("/host").appContextFactory(servlet -> {
            return Hasor.create(servlet).mainSettingWith("empty-scan.xml").build((WebModule) binder -> {
                binder.setEncodingCharacter("UTF-8", "UTF-8");
                binder.mappingTo("/mvc").with(Action.class);
                binder.mappingTo("/async").with(AsyncAction.class);
                binder.mappingTo("/void").with(VoidAction.class);
                binder.mappingTo("/void-async").with(AsyncVoidAction.class);
                binder.mappingTo("/direct").with(DirectResponseAction.class);
                binder.mappingTo("/direct-async").with(AsyncDirectResponseAction.class);
                binder.addRender("manual").toProvider(() -> (invoker, writer) -> writer.write("must not render"));
                binder.filter("/*").through((invoker, chain) -> {
                    assertEquals("/host", invoker.getHttpRequest().getContextPath());
                    this.events.add("filter.before");
                    invoker.getHttpResponse().setHeader("X-Filtered", "true");
                    try {
                        Object value = chain.doNext(invoker);
                        this.events.add("filter.after");
                        return value;
                    } finally {
                        this.completedRequests.add(true);
                    }
                });
                for (String name : List.of("first", "second")) {
                    binder.bindInterceptor(new HandlerInterceptor() {
                        @Override
                        public boolean preHandle(Invoker invoker) {
                            MvcInterceptionIntegrationTest.this.events.add(name + ".pre");
                            if (name.equals("second") && invoker.getHttpRequest().getParameter("deny") != null) {
                                invoker.getHttpResponse().setStatus(401);
                                return false;
                            }
                            return true;
                        }

                        @Override
                        public void postHandle(Invoker invoker, Object result) {
                            MvcInterceptionIntegrationTest.this.events.add(name + ".post");
                        }

                        @Override
                        public void afterCompletion(Invoker invoker, Throwable failure) {
                            String outcome = failure == null ? "complete" : "failed";
                            MvcInterceptionIntegrationTest.this.events.add(name + "." + outcome);
                        }
                    });
                }
                binder.addExceptionHandler(IOException.class, (invoker, failure) -> {
                    this.events.add("handler");
                    if (!recover) {
                        return null;
                    }
                    invoker.getHttpResponse().setStatus(409);
                    return "recovered";
                });
            });
        });
        this.server = WebServers.create(config);
        this.server.start();
    }

    @After
    public void stop() throws Exception {
        if (this.server != null) {
            this.server.stop();
        }
    }

    private String request(String path, int status) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + this.server.getPort() + "/host" + path).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        try {
            assertEquals(status, connection.getResponseCode());
            if (status < 500) {
                assertEquals("true", connection.getHeaderField("X-Filtered"));
            }
            try (InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                String body = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertNotNull("Server callbacks did not complete", this.completedRequests.poll(3, TimeUnit.SECONDS));
                return body;
            }
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void normalAndAsyncRequestsRunOrderedMvcCallbacksAndRender() throws Exception {
        this.start(true);
        for (String path : List.of("/mvc", "/async")) {
            this.events.clear();
            assertEquals("ok", this.request(path, 200));
            assertEquals(List.of("filter.before", "first.pre", "second.pre", "second.post", "first.post", "second.complete", "first.complete", "filter.after"), this.events);
        }
    }

    @Test
    public void directResponsesSkipRenderingInNormalAndAsyncRequests() throws Exception {
        this.start(true);
        for (String path : List.of("/direct", "/direct-async")) {
            this.events.clear();
            assertEquals("", this.request(path, 202));
            assertEquals(List.of("filter.before", "first.pre", "second.pre", "second.post", "first.post", "second.complete", "first.complete", "filter.after"), this.events);
            assertEquals("ok", this.request("/mvc", 200));
        }
    }

    @Test
    public void markedResponsesSkipRenderingInNormalAndAsyncRequests() throws Exception {
        this.start(true);
        for (String path : List.of("/mvc", "/async")) {
            this.events.clear();
            assertEquals("", this.request(path + "?handled=true", 200));
            assertEquals(List.of("filter.before", "first.pre", "second.pre", "second.post", "first.post", "second.complete", "first.complete", "filter.after"), this.events);
            assertEquals("ok", this.request(path, 200));
        }
    }

    @Test
    public void deniedRequestsStopMvcAndOnlyEnteredInterceptorsComplete() throws Exception {
        this.start(true);
        assertEquals("", this.request("/mvc?deny=true", 401));
        assertEquals(List.of("filter.before", "first.pre", "second.pre", "first.complete", "filter.after"), this.events);
    }

    @Test
    public void resolvedFailuresRenderForBothNormalAndAsyncRequests() throws Exception {
        this.start(true);
        for (String path : List.of("/mvc", "/async", "/void", "/void-async")) {
            this.events.clear();
            assertEquals("recovered", this.request(path + "?fail=true", 409));
            assertEquals(List.of("filter.before", "first.pre", "second.pre", "handler", "second.complete", "first.complete", "filter.after"), this.events);
        }
    }

    @Test
    public void unresolvedFailuresReachTheContainerForBothNormalAndAsyncRequests() throws Exception {
        this.start(false);
        for (String path : List.of("/mvc", "/async", "/void", "/void-async")) {
            this.events.clear();
            this.request(path + "?fail=true", 500);
            List<String> snapshot = new ArrayList<>(this.events);
            assertEquals(1, snapshot.stream().filter("handler"::equals).count());
            assertTrue(snapshot.contains("second.failed"));
            assertTrue(snapshot.contains("first.failed"));
        }
    }
}
