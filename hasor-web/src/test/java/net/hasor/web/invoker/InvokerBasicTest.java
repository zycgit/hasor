/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.test.web.actions.async.ErrorAsyncAction;
import net.hasor.test.web.actions.async.MethodAsyncAction;
import net.hasor.test.web.actions.render.HtmlProduces;
import net.hasor.test.web.render.SimpleRenderEngine;
import net.hasor.web.AbstractTest;
import net.hasor.web.Mapping;
import net.hasor.web.WebApiBinder;
import net.hasor.web.binder.MappingDef;
import net.hasor.web.binder.OneConfig;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class InvokerBasicTest extends AbstractTest {
    @Test
    public void contentType_1() throws Throwable {
        SimpleRenderEngine renderEngine = new SimpleRenderEngine();
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.addRender("html").toInstance(renderEngine);
            apiBinder.loadMappingTo(HtmlProduces.class);
        }, servlet30("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        //
        List<MappingDef> definitions = appContext.findBindingBean(MappingDef.class);
        assertEquals(2, definitions.size());
        final Set<String> responseType = new HashSet<>();
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            responseType.add(invocation.getArguments()[0].toString());
            return null;
        }).when(servletResponse).setContentType(anyString());
        //
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        {
            ExecuteCaller caller = invokerContext.genCaller(mockRequest("post", new URL("http://www.hasor.net/abc.do")), servletResponse);
            caller.invoke(null);
            assertTrue(responseType.contains("test/html"));
        }
        //
        {
            responseType.clear();
            ExecuteCaller caller = invokerContext.genCaller(mockRequest("get", new URL("http://www.hasor.net/abc.do")), servletResponse);
            caller.invoke(null);
            assertTrue(responseType.contains("text/javacc_jj"));
        }
    }

    /* 无任何匹配的请求，不报错 */
    @Test
    public void none_matching_test_1() throws Throwable {
        MethodAsyncAction action = new MethodAsyncAction();
        //
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/abc.do").with(action);
        }, servlet25("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        HttpServletRequest servletRequest = mockRequest("post", new URL("http://www.hasor.net/abcefg.do"));
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        ExecuteCaller caller = invokerContext.genCaller(servletRequest, servletResponse);
        caller.invoke(null).get();
        assertFalse(action.isExecute());
    }

    @Test
    public void asyncInvocationWorker_test_1() {
        AsyncContext asyncContext = PowerMockito.mock(AsyncContext.class);
        final Method targetMethod = reflectMethod("asyncInvocationWorker_test_1");
        //
        AsyncInvocationWorker worker = new AsyncInvocationWorker(asyncContext, targetMethod) {
            @Override
            public void doWork(Method method) {
                assertSame(targetMethod, method);
            }

            @Override
            public void doWorkWhenError(Method targetMethod, Throwable e) {
                fail();
            }
        };
        //
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        PowerMockito.doAnswer((Answer<Void>) invocationOnMock -> {
            atomicBoolean.set(true);
            return null;
        }).when(asyncContext).complete();
        //
        worker.run();
        assertTrue(atomicBoolean.get());
        verify(asyncContext).complete();
        verify(asyncContext, never()).dispatch();
    }

    @Test
    public void asyncInvocationWorker_test_2() {
        AsyncContext asyncContext = PowerMockito.mock(AsyncContext.class);
        final Method targetMethod = reflectMethod("asyncInvocationWorker_test_2");
        final Exception error = new Exception();
        //
        AsyncInvocationWorker worker = new AsyncInvocationWorker(asyncContext, targetMethod) {
            @Override
            public void doWork(Method method) throws Throwable {
                throw error;
            }

            @Override
            public void doWorkWhenError(Method method, Throwable e) {
                assertSame(targetMethod, method);
                assertSame(error, e);
                verify(asyncContext, never()).complete();
                verify(asyncContext, never()).dispatch();
            }
        };
        //
        worker.run();
        verify(asyncContext).dispatch();
        verify(asyncContext, never()).complete();
    }

    @Test
    public void asyncAction_test_1() throws Throwable {
        MethodAsyncAction action = new MethodAsyncAction();
        //
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/abc.do").with(action);
        }, servlet30("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        HttpServletRequest servletRequest = mockRequest("post", new URL("http://www.hasor.net/abc.do"));
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        //
        Object obj = new Object();
        action.getData().set(obj);
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        ExecuteCaller caller = invokerContext.genCaller(servletRequest, servletResponse);
        caller.invoke(null).get();
        assertSame(obj, action.getData().get());
    }

    @Test
    public void asyncAction_test_2() throws Throwable {
        MethodAsyncAction action = new MethodAsyncAction();
        //
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/abc.do").with(action);
        }, servlet25("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        HttpServletRequest servletRequest = mockRequest("post", new URL("http://www.hasor.net/abc.do"));
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        //
        Object obj = new Object();
        action.getData().set(obj);
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        ExecuteCaller caller = invokerContext.genCaller(servletRequest, servletResponse);
        caller.invoke(null).get();
        assertNotSame(obj, action.getData().get());
    }

    @Test
    public void error_action_test_async() throws Throwable {
        ErrorAsyncAction action = new ErrorAsyncAction();
        //
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/abc.do").with(action);
        }, servlet30("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        HttpServletRequest servletRequest = mockRequest("post", new URL("http://www.hasor.net/abc.do"));
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        //
        Object obj = new Object();
        action.getData().set(obj);
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        ExecuteCaller caller = invokerContext.genCaller(servletRequest, servletResponse);
        try {
            caller.invoke(null).get();
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("aaaa", e.getCause().getMessage());
        } finally {
            assertSame(obj, action.getData().get()); // 异步
        }
    }

    @Test
    public void error_action_test_sync() throws Throwable {
        ErrorAsyncAction action = new ErrorAsyncAction();
        //
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", Hasor::create, apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/abc.do").with(action);
        }, servlet25("/"), AbstractTest.LoadModule.Web, AbstractTest.LoadModule.Render);
        //
        HttpServletRequest servletRequest = mockRequest("post", new URL("http://www.hasor.net/abc.do"));
        HttpServletResponse servletResponse = PowerMockito.mock(HttpServletResponse.class);
        //
        Object obj = new Object();
        action.getData().set(obj);
        //
        InvokerContext invokerContext = new InvokerContext();
        invokerContext.initContext(appContext, new OneConfig("", () -> appContext));
        ExecuteCaller caller = invokerContext.genCaller(servletRequest, servletResponse);
        try {
            caller.invoke(null).get();
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("aaaa", e.getCause().getMessage());
        } finally {
            assertNotSame(obj, action.getData().get()); // 同步
        }
    }

    @Test
    public void invokerSupplier_test_1() throws Throwable {
        AppContext appContext = PowerMockito.mock(AppContext.class);
        HttpServletRequest httpRequest = super.mockRequest("get", new URL("http://www.hasor.net/query_param.do?byteParam=123&bigInteger=321"));
        HttpServletResponse httpResponse = PowerMockito.mock(HttpServletResponse.class);
        InvokerSupplier supplier = new InvokerSupplier(PowerMockito.mock(Mapping.class), appContext, httpRequest, httpResponse);
        //
        assertSame(httpRequest, supplier.getHttpRequest());
        assertSame(httpResponse, supplier.getHttpResponse());
        assertSame(appContext, supplier.getAppContext());
        //
        supplier.put("abc", "abc");
        assertEquals("abc", supplier.get("abc"));
        supplier.remove("abc");
        assertNull(supplier.get("abc"));
        //
        supplier.put("key", "kv");
        assertEquals("kv", supplier.get("key"));
        supplier.lockKey("key");
        try {
            supplier.put("key", "111");
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().endsWith(" is lock key."));
        }
        try {
            supplier.remove("key");
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().endsWith(" is lock key."));
        }
        //
        Set<String> strings = supplier.keySet();
        assertTrue(strings.contains("key"));
    }

    private static Method reflectMethod(String methodName) {
        try {
            return InvokerBasicTest.class.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
