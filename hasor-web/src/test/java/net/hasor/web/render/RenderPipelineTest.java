package net.hasor.web.render;
import static org.junit.Assert.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.binder.FilterDef;
import net.hasor.web.binder.OneConfig;
import net.hasor.web.invoker.InvokerContext;

public class RenderPipelineTest extends AbstractTest {
    public static class Action {
        @Get
        @RenderType("text")
        public String get(Invoker invoker) {
            @SuppressWarnings("unchecked") List<String> events = (List<String>) invoker.get("events");
            if (events != null) {
                events.add("action");
            }
            return "ok";
        }
    }

    public static class FailedAction {
        @Get
        public String get() {
            throw new IllegalStateException("action failed");
        }
    }

    @Test
    public void renderingRunsInsideBusinessFilterTerminal() throws Throwable {
        List<String> events = new ArrayList<>();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(Action.class);
            binder.filter("/*").through(Integer.MIN_VALUE, (invoker, chain) -> {
                invoker.put("events", events);
                events.add("before");
                Object result = chain.doNext(invoker);
                events.add("after");
                return result;
            });
            binder.addRender("text").toProvider(() -> (invoker, writer) -> {
                events.add("render");
                writer.write("rendered");
            });
        })) {
            assertEquals(1, app.findBindingBean(FilterDef.class).size());
            assertEquals(1, app.findBindingBean(RenderProcessor.class).size());
            assertEquals("rendered", mockAndCallHttp("get", "http://localhost/test", app));
            assertEquals(List.of("before", "action", "render", "after"), events);
        }
    }

    @Test
    public void filterRecoveryDoesNotAutomaticallyRender() throws Throwable {
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(FailedAction.class);
            binder.filter("/*").through((invoker, chain) -> {
                try {
                    return chain.doNext(invoker);
                } catch (IllegalStateException e) {
                    invoker.put(Invoker.RETURN_DATA_KEY, "recovered");
                    return "recovered";
                }
            });
        })) {
            assertEquals("", mockAndCallHttp("get", "http://localhost/test", app));
        }
    }

    @Test
    public void unhandledExceptionDoesNotRunRenderer() throws Throwable {
        AtomicInteger renders = new AtomicInteger();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(FailedAction.class);
            binder.addRender("text").toProvider(() -> (invoker, writer) -> renders.incrementAndGet());
        })) {
            try {
                mockAndCallHttp("get", "http://localhost/test", app);
                fail("Expected failure");
            } catch (java.util.concurrent.ExecutionException expected) {
                assertTrue(expected.getCause() instanceof IllegalStateException);
            }
            assertEquals(0, renders.get());
        }
    }

    @Test
    public void fixedInfrastructureIsResolvedOnlyAtContextInitialization() throws Throwable {
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(Action.class);
        })) {
            AppContext tracked = org.mockito.Mockito.spy(app);
            InvokerContext context = new InvokerContext();
            context.initContext(tracked, new OneConfig("", () -> tracked));
            for (String path : List.of("/test", "/test", "/unmatched", "/unmatched")) {
                HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);
                mockRenderResponse(response);
                context.genCaller(mockRequest("GET", new URL("http://localhost" + path)), response).invoke(null).get();
            }
            org.mockito.Mockito.verify(tracked, org.mockito.Mockito.times(1)).getInstance(RenderProcessor.class);
            org.mockito.Mockito.verify(tracked, org.mockito.Mockito.times(1)).getInstance(net.hasor.web.binder.ResourceDef[].class);
            org.mockito.Mockito.verify(tracked, org.mockito.Mockito.times(1)).getInstance(net.hasor.web.ServletVersion.class);
        }
    }

    @Test
    public void shortCircuitFilterDoesNotRenderReturnedValue() throws Throwable {
        AtomicInteger renders = new AtomicInteger();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(Action.class);
            binder.filter("/*").through((invoker, chain) -> "short circuit");
            binder.addRender("text").toProvider(() -> (invoker, writer) -> renders.incrementAndGet());
        })) {
            assertEquals("", mockAndCallHttp("GET", "http://localhost/test", app));
            assertEquals(0, renders.get());
        }
    }

    @Test
    public void asynchronousOwnershipSkipsResponseRendering() throws Throwable {
        AtomicInteger renders = new AtomicInteger();
        try (AppContext app = Hasor.create(servlet30("/")).build((WebModule) binder -> {
            binder.mappingTo("/test").with(Action.class);
            binder.addRender("text").toProvider(() -> (invoker, writer) -> renders.incrementAndGet());
        })) {
            HttpServletRequest request = mockRequest("get", new URL("http://localhost/test"));
            PowerMockito.when(request.isAsyncStarted()).thenReturn(true);
            HttpServletResponse response = PowerMockito.mock(HttpServletResponse.class);
            InvokerContext context = new InvokerContext();
            context.initContext(app, new OneConfig("", () -> app));
            assertEquals("ok", context.genCaller(request, response).invoke(null).get());
            assertEquals(0, renders.get());
        }
    }
}
