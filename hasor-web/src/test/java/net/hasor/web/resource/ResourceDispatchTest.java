/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.hasor.cobble.loader.providers.PathResourceLoader;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.HandlerInterceptor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.binder.FilterDef;
import net.hasor.web.binder.ResourceBinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ResourceDispatchTest extends AbstractTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    public static class Action {
        @Get
        public String get() {
            return "action";
        }
    }

    private Path directory(String name, String content) throws Exception {
        Path root = temporary.newFolder(name).toPath();
        Files.writeString(root.resolve("app.txt"), content);
        Files.writeString(root.resolve("index.html"), "welcome");
        return root;
    }

    @Test
    public void standaloneWebServesResourcesWithoutResourceFilters() throws Throwable {
        Path root = directory("static", "asset");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile()));
        })) {
            assertTrue(app.findBindingBean(FilterDef.class).isEmpty());
            assertEquals("asset", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
            assertEquals("welcome", mockAndCallHttp("GET", "http://localhost/assets/", app));
            assertEquals("", mockAndCallHttp("HEAD", "http://localhost/assets/app.txt", app));
        }
    }

    @Test
    public void multipleLoadersWorkThroughWrapperInDeclaredOrder() throws Throwable {
        Path first = directory("first", "first");
        Path second = directory("second", "second");
        Files.writeString(second.resolve("only-second.txt"), "fallback");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            net.hasor.web.WebApiBinder wrapped = new net.hasor.web.wrap.WebApiBinderWrap(binder);
            ResourceBinder binding = wrapped.addResource("/assets/**", new PathResourceLoader(first.toFile()), new PathResourceLoader(second.toFile()));
            assertSame(binding, binding.welcomeFile("index.html").cacheControl("no-cache").fallbackPaths("/assets/tasks/*").excludedPrefixes("/assets/private").order(-1));
        })) {
            assertEquals("first", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
            assertEquals("fallback", mockAndCallHttp("GET", "http://localhost/assets/only-second.txt", app));
            assertEquals("welcome", mockAndCallHttp("GET", "http://localhost/assets/tasks/123", app));
        }
    }

    @Test
    public void disabledWelcomeDoesNotBreakMissingResources() throws Throwable {
        Path root = directory("no-welcome", "asset");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/**", new PathResourceLoader(root.toFile())).welcomeFile(null).fallbackPaths("/tasks/*");
        })) {
            assertEquals("", mockAndCallHttp("GET", "http://localhost/", app));
            assertEquals("", mockAndCallHttp("GET", "http://localhost/tasks/123", app));
            assertEquals("asset", mockAndCallHttp("GET", "http://localhost/app.txt", app));
        }
    }

    @Test
    public void actionWinsEvenWithHighestResourcePriority() throws Throwable {
        Path root = directory("collision", "asset");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile())).order(Integer.MIN_VALUE);
            binder.mappingTo("/assets/app.txt").with(Action.class);
        })) {
            assertEquals("action", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
        }
    }

    @Test
    public void resourcesUseHttpFiltersButBypassMvcInterceptors() throws Throwable {
        Path root = directory("filtered", "asset");
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile()));
            binder.bindInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(Invoker invoker) {
                    fail("Static resources must bypass MVC interceptors");
                    return false;
                }
            });
            binder.filter("/*").through((invoker, chain) -> {
                events.add("before");
                if (invoker.getRequestPath().endsWith("index.html")) {
                    invoker.getHttpResponse().setStatus(403);
                    return null;
                }
                Object result = chain.doNext(invoker);
                events.add("after");
                return result;
            });
        })) {
            assertEquals(1, app.findBindingBean(FilterDef.class).size());
            assertEquals("asset", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
            assertEquals(List.of("before", "after"), events);
            events.clear();
            assertEquals("", mockAndCallHttp("GET", "http://localhost/assets/index.html", app));
            assertEquals(List.of("before"), events);
        }
    }

    @Test
    public void matchedMissingResourceIs404AndOnlyUnmatchedPathsContinue() throws Throwable {
        Path root = directory("missing", "asset");
        Path lower = directory("lower", "other");
        List<String> filteredPaths = new ArrayList<>();
        Files.writeString(lower.resolve("missing.txt"), "must not be served");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile()));
            binder.addResource("/assets/**", new PathResourceLoader(lower.toFile())).order(1);
            binder.filter("/*").through((invoker, chain) -> {
                assertNull(invoker.ownerMapping());
                invoker.contentType();
                filteredPaths.add(invoker.getRequestPath());
                return chain.doNext(invoker);
            });
        })) {
            net.hasor.web.invoker.InvokerContext context = new net.hasor.web.invoker.InvokerContext();
            context.initContext(app, new net.hasor.web.binder.OneConfig("", () -> app));
            java.util.concurrent.atomic.AtomicInteger continuations = new java.util.concurrent.atomic.AtomicInteger();
            javax.servlet.FilterChain chain = (request, response) -> continuations.incrementAndGet();
            javax.servlet.http.HttpServletResponse missing = org.mockito.Mockito.mock(javax.servlet.http.HttpServletResponse.class);
            context.genCaller(mockRequest("GET", new java.net.URL("http://localhost/assets/missing.txt")), missing).invoke(chain).get();
            org.mockito.Mockito.verify(missing).sendError(404);
            assertEquals(0, continuations.get());
            javax.servlet.http.HttpServletResponse unmatched = org.mockito.Mockito.mock(javax.servlet.http.HttpServletResponse.class);
            context.genCaller(mockRequest("GET", new java.net.URL("http://localhost/other")), unmatched).invoke(chain).get();
            assertEquals(1, continuations.get());
            assertEquals(List.of("/assets/missing.txt", "/other"), filteredPaths);
        }
    }

    @Test
    public void specificMappingsWinTiesAndOrderIsIndependent() throws Throwable {
        Path broad = directory("broad", "broad");
        Files.createDirectory(broad.resolve("assets"));
        Files.writeString(broad.resolve("assets/app.txt"), "broad");
        Path specific = directory("specific", "specific");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/**", new PathResourceLoader(broad.toFile()));
            binder.addResource("/assets/**", new PathResourceLoader(specific.toFile()));
        })) {
            assertEquals("specific", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
        }
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/**", new PathResourceLoader(broad.toFile())).order(-1);
            binder.addResource("/assets/**", new PathResourceLoader(specific.toFile()));
        })) {
            assertEquals("broad", mockAndCallHttp("GET", "http://localhost/assets/app.txt", app));
        }
    }

    @Test
    public void explicitSpaFallbackDoesNotSwallowExcludedOrMissingAssets() throws Throwable {
        Path root = directory("spa", "asset");
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/**", new PathResourceLoader(root.toFile())).fallbackPaths("/tasks/*", "/api/*").excludedPrefixes("/api");
        })) {
            assertEquals("welcome", mockAndCallHttp("GET", "http://localhost/tasks/123", app));
            assertEquals("", mockAndCallHttp("GET", "http://localhost/api/123", app));
            assertEquals("", mockAndCallHttp("GET", "http://localhost/tasks/missing.js", app));
        }
    }
}
