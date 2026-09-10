package net.hasor.web.render;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import net.hasor.web.Invoker;
import net.hasor.web.Mapping;

public class RedirectToTest {
    @RedirectTo(301)
    public static class Actions {
        public String permanent() {
            return "/target";
        }

        @RedirectTo
        public String temporary() {
            return "/target";
        }

        @RedirectTo(303)
        public String seeOther() {
            return "/target";
        }

        @RedirectTo(307)
        public String preserveMethod() {
            return "/target";
        }

        @RedirectTo(308)
        public String permanentPreserveMethod() {
            return "/target";
        }

        @RedirectTo(200)
        public String invalid() {
            return "/target";
        }
    }

    private void render(String method, String location, HttpServletResponse response) throws Throwable {
        RenderInvoker invoker = mock(RenderInvoker.class);
        Mapping mapping = mock(Mapping.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(invoker.ownerMapping()).thenReturn(mapping);
        when(invoker.getHttpRequest()).thenReturn(request);
        when(mapping.findMethod(request)).thenReturn(Actions.class.getMethod(method));
        when(invoker.getHttpResponse()).thenReturn(response);
        when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(location);
        when(response.encodeRedirectURL(location)).thenReturn(location);
        new RedirectTo.RedirectRenderEngine().process(invoker, new StringWriter());
    }

    @Test
    public void methodDefaultOverridesClassPermanentRedirect() throws Throwable {
        HttpServletResponse response = mock(HttpServletResponse.class);
        render("temporary", "/target", response);
        verify(response).sendRedirect("/target");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void supportsExplicitStatusesAndClassDefault() throws Throwable {
        String[] methods = { "permanent", "seeOther", "preserveMethod", "permanentPreserveMethod" };
        int[] codes = { 301, 303, 307, 308 };
        for (int i = 0; i < methods.length; i++) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            render(methods[i], "/target", response);
            verify(response).setStatus(codes[i]);
            verify(response).setHeader("Location", "/target");
            verify(response).flushBuffer();
            verify(response, never()).sendRedirect(anyString());
        }
    }

    @Test
    public void rejectsUnsupportedStatusAndUnsafeLocation() throws Throwable {
        HttpServletResponse response = mock(HttpServletResponse.class);
        try {
            render("invalid", "/target", response);
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("200"));
        }
        try {
            render("permanent", "/target\r\nInjected: value", response);
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("CR or LF"));
        }
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    public void committedResponseIsNotChanged() throws Throwable {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.isCommitted()).thenReturn(true);
        render("permanent", "/target", response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).flushBuffer();
    }
}
