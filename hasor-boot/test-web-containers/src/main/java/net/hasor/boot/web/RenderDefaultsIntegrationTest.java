package net.hasor.boot.web;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Test;

import net.hasor.boot.fixtures.render.RenderApplication;
import net.hasor.core.Hasor;

public abstract class RenderDefaultsIntegrationTest extends ContainerIntegrationTest {
    private WebServer server;

    private void start(WebServerConfig config) throws Exception {
        if (config.getHconfigFile() == null) {
            config.hconfigFile("render-scan.xml");
        }
        this.server = WebServers.create(config.port(0));
        this.server.start();
    }

    @After
    public void stop() throws Exception {
        if (this.server != null) {
            this.server.stop();
        }
    }

    private HttpURLConnection request(String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + this.server.getPort() + path).openConnection();
        c.setConnectTimeout(3000);
        c.setReadTimeout(3000);
        return c;
    }

    private String get(String path, String mime, String charset) throws Exception {
        HttpURLConnection c = request(path);
        try {
            assertEquals(200, c.getResponseCode());
            if (mime != null) {
                assertTrue(c.getContentType(), c.getContentType().contains(mime));
            }
            byte[] body = c.getInputStream().readAllBytes();
            if (body.length > 0 && charset != null) {
                assertTrue(c.getContentType(), c.getContentType().toUpperCase().contains(charset.toUpperCase()));
                assertEquals(body.length, c.getContentLength());
            }
            return new String(body, Charset.forName(charset == null ? "UTF-8" : charset));
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void rendersObjectsCollectionsStringsAndRawJson() throws Exception {
        start(WebServerConfig.of(RenderApplication.class));
        assertEquals("{\"name\":\"中文\"}", get("/render/object", "application/json", "UTF-8"));
        assertEquals("[1,2]", get("/render/list", "application/json", "UTF-8"));
        assertEquals("{\"name\":\"中文\",\"count\":2}", get("/render/pojo", "application/json", "UTF-8"));
        assertEquals("中文", get("/render/text", "text/plain", "UTF-8"));
        assertEquals("{\"name\":\"中文\"}", get("/render/raw", "application/json", "UTF-8"));
        assertEquals("{\"name\":\"中文\"}", get("/render/media", "application/problem+json", "UTF-8"));
        assertEquals("中文", get("/render/charset", "text/plain", "GB18030"));
    }

    @Test
    public void doesNotAppendToManualOrEmptyResponses() throws Exception {
        start(WebServerConfig.of(RenderApplication.class));
        assertEquals("", get("/render/null", null, null));
        assertEquals("", get("/render/void", null, null));
        assertEquals("manual", get("/render/written", null, null));
        assertEquals("AB", get("/render/stream", null, null));
        HttpURLConnection c = request("/render/no-content");
        try {
            assertEquals(204, c.getResponseCode());
            assertEquals(0, c.getInputStream().readAllBytes().length);
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void xmlDefaultsAndAnnotationsControlRendering() throws Exception {
        start(WebServerConfig.of(Hasor.create().mainSettingWith("render-hconfig.xml").buildSettings(), RenderApplication.class).hconfigFile("render-hconfig.xml"));
        assertEquals("\"中文\"", get("/render/text", "application/json", "UTF-8"));
        assertEquals("{\"name\":\"中文\"}", get("/render/raw", "application/json", "UTF-8"));
        HttpURLConnection c = request("/render/object");
        try {
            assertEquals(200, c.getResponseCode());
            assertEquals("no-store", c.getHeaderField("Cache-Control"));
        } finally {
            c.disconnect();
        }
        c = request("/render/cache");
        try {
            assertEquals(200, c.getResponseCode());
            assertEquals("private", c.getHeaderField("Cache-Control"));
        } finally {
            c.disconnect();
        }
        c = request("/app.css");
        try {
            assertEquals(200, c.getResponseCode());
            assertNotEquals("no-store", c.getHeaderField("Cache-Control"));
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void defaultTextAndHeadHasNoBody() throws Exception {
        start(WebServerConfig.of(RenderApplication.class));
        assertEquals("中文", get("/render/text", "text/plain", "UTF-8"));
        HttpURLConnection c = request("/render/object");
        c.setRequestMethod("HEAD");
        try {
            assertEquals(200, c.getResponseCode());
            assertTrue(c.getContentLength() > 0);
            assertEquals(0, c.getInputStream().readAllBytes().length);
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void disablingDefaultsStillAllowsExplicitRender() throws Exception {
        start(WebServerConfig.of(RenderApplication.class).hconfigFile("render-none.xml"));
        assertEquals("", get("/render/object", null, null));
        assertEquals("{\"name\":\"中文\"}", get("/render/raw", "application/json", "UTF-8"));
    }

    @Test
    public void registeredRenderersOverrideBuiltinsAndCanBeSelectedByName() throws Exception {
        start(WebServerConfig.of(RenderApplication.CustomModule.class).hconfigFile("render-custom.xml"));
        assertEquals("\"custom-json\"", get("/render/object", "application/json", "UTF-8"));
        assertEquals("custom-engine", get("/render/text", "application/octet-stream", "UTF-8"));
        assertEquals("{\"name\":\"中文\"}", get("/render/raw", "application/json", "UTF-8"));
    }

    @Test
    public void responseFallsBackToRequestEncoding() throws Exception {
        start(WebServerConfig.of(RenderApplication.RequestEncodingModule.class));
        assertEquals("中文", get("/render/text", "text/plain", "GB18030"));
    }

    @Test
    public void binderResponseEncodingWinsOverRequest() throws Exception {
        start(WebServerConfig.of(RenderApplication.ResponseEncodingModule.class));
        assertEquals("中文", get("/render/text", "text/plain", "UTF-16LE"));
        assertEquals("中文", get("/render/charset", "text/plain", "GB18030"));
        assertEquals("中文", get("/render/api-charset", "text/plain", "GB18030"));
    }

    @Test
    public void explicitNoneDoesNotProduceContentType() throws Exception {
        start(WebServerConfig.of(RenderApplication.class));
        HttpURLConnection c = request("/render/none");
        try {
            assertEquals(200, c.getResponseCode());
            assertEquals(0, c.getInputStream().readAllBytes().length);
            assertNull(c.getContentType());
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void unknownEngineFailsStartup() throws Exception {
        try {
            start(WebServerConfig.of(RenderApplication.class).hconfigFile("render-missing.xml"));
            fail("Unknown renderer accepted");
        } catch (Exception expected) {
            assertFalse(this.server.isStart());
        }
    }

    @Test
    public void redirectsPreserveSelectedStatusAndLocation() throws Exception {
        start(WebServerConfig.of(RenderApplication.class));
        String[] paths = { "default", "permanent", "override" };
        int[] codes = { 302, 301, 302 };
        for (int i = 0; i < paths.length; i++) {
            HttpURLConnection c = request("/render/redirect-" + paths[i]);
            c.setInstanceFollowRedirects(false);
            try {
                assertEquals(codes[i], c.getResponseCode());
                assertTrue(c.getHeaderField("Location").endsWith("/render/text"));
                assertNotEquals("application/octet-stream", c.getContentType());
            } finally {
                c.disconnect();
            }
        }
    }
}
