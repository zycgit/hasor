/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web.cors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CorsFilterTest {
    @Test
    public void migratedFilterPreservesActualRequestBehavior() throws Throwable {
        Invoker invoker = mock(Invoker.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        InvokerChain chain = mock(InvokerChain.class);
        when(invoker.getHttpRequest()).thenReturn(request);
        when(invoker.getHttpResponse()).thenReturn(response);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://example.org");
        when(chain.doNext(invoker)).thenReturn("result");
        assertEquals("result", new CorsFilter().doInvoke(invoker, chain));
        verify(response).setHeader("Access-Control-Allow-Origin", "https://example.org");
        verify(response).setHeader("Access-Control-Allow-Credentials", "true");
        verify(chain).doNext(invoker);
    }

    @Test
    public void migratedFilterStillShortCircuitsOptions() throws Throwable {
        Invoker invoker = mock(Invoker.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        InvokerChain chain = mock(InvokerChain.class);
        when(invoker.getHttpRequest()).thenReturn(request);
        when(invoker.getHttpResponse()).thenReturn(response);
        when(request.getMethod()).thenReturn("OPTIONS");
        assertNull(new CorsFilter().doInvoke(invoker, chain));
        verify(response).setHeader("Access-Control-Allow-Origin", "*");
        verify(chain, never()).doNext(invoker);
    }
}
