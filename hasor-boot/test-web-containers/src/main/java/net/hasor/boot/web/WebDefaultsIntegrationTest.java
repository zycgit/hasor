/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.hasor.boot.fixtures.defaults.DefaultApplication;
import net.hasor.boot.fixtures.defaults.HelloController;
import net.hasor.boot.fixtures.manual.ManualApplication;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/** The same HTTP contract runs against Tomcat, Jetty and Undertow. */
public abstract class WebDefaultsIntegrationTest extends ContainerIntegrationTest {
    private WebServer server;

    private void start(WebServerConfig config) throws Exception {
        if (config.getHconfigFile() == null) {
            config.hconfigFile("web-scan.xml");
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

    private HttpURLConnection request(String path, String method) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + this.server.getPort() + path).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod(method);
        return connection;
    }

    private String get(String path, int status, String mime) throws Exception {
        HttpURLConnection connection = request(path, "GET");
        try {
            assertEquals(path, status, connection.getResponseCode());
            if (mime != null) {
                assertTrue(connection.getContentType(), connection.getContentType().contains(mime));
            }
            if (status != 200) {
                return "";
            }
            try (InputStream input = connection.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void defaultsServeHomeAssetsAndScanApplicationPackage() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class));
        assertEquals("你好", get("/hello", 200, null));
        assertEquals("你好", get("/override", 200, null));
        assertTrue(get("/", 200, "text/html").contains("Boot welcome"));
        assertTrue(get("/app.css", 200, "text/css").contains("black"));
        assertTrue(get("/app.js", 200, "javascript").contains("Boot"));
        get("/private-config.txt", 404, null);
        get("/unknown", 404, null);
    }

    @Test
    public void explicitSpaPathsDoNotMaskApiOrMissingAssets() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class).spaPaths("/app/*", "/api/*"));
        assertTrue(get("/app/details", 200, "text/html").contains("Boot welcome"));
        get("/app/missing.js", 404, null);
        get("/api/missing", 404, null);
        assertTrue(get("/health", 200, "application/json").contains("UP"));
    }

    @Test
    public void coreScopeAndStaticResourcesCanBeConfiguredIndependently() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class).hconfigFile("empty-scan.xml").staticResources(false));
        get("/hello", 404, null);
        assertNoWelcomePage();
    }

    @Test
    public void explicitScanExcludesAndLocationOverride() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class).excludeScan(HelloController.class).staticLocation("other-public"));
        get("/hello", 404, null);
        assertNoWelcomePage();
    }

    private void assertNoWelcomePage() throws Exception {
        HttpURLConnection connection = request("/", "GET");
        try {
            // With no Boot resource, Undertow denies the document-root directory (403);
            // Tomcat/Jetty report no resource (404). Neither may serve the classpath index.
            int status = connection.getResponseCode();
            assertTrue(status == 403 || status == 404);
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void manualRegistrationSurvivesAutoScanAndContextPath() throws Exception {
        start(WebServerConfig.of(ManualApplication.class).contextPath("/console"));
        assertEquals("你好", get("/console/hello", 200, null));
        assertTrue(get("/console/", 200, "text/html").contains("Boot welcome"));
        get("/console/app.css", 200, "text/css");
    }

    @Test
    public void conflictingAutoScannedMappingsFailStartup() throws Exception {
        try {
            start(WebServerConfig.of(DefaultApplication.class).hconfigFile("conflict-scan.xml"));
            fail("Conflicting routes must fail startup");
        } catch (Exception expected) {
            assertFalse(this.server.isStart());
        }
    }

    @Test
    public void headMethodsAndConditionalRequests() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class));
        HttpURLConnection head = request("/app.css", "HEAD");
        try {
            assertEquals(200, head.getResponseCode());
            assertEquals("nosniff", head.getHeaderField("X-Content-Type-Options"));
            assertEquals(0, head.getInputStream().readAllBytes().length);
            String modified = head.getHeaderField("Last-Modified");
            assertNotNull(modified);
            HttpURLConnection conditional = request("/app.css", "GET");
            try {
                conditional.setRequestProperty("If-Modified-Since", modified);
                assertEquals(304, conditional.getResponseCode());
            } finally {
                conditional.disconnect();
            }
        } finally {
            head.disconnect();
        }
        HttpURLConnection post = request("/", "POST");
        try {
            assertEquals(405, post.getResponseCode());
            assertEquals("GET, HEAD", post.getHeaderField("Allow"));
        } finally {
            post.disconnect();
        }
    }

    @Test
    public void rejectsTraversalAndHiddenFiles() throws Exception {
        start(WebServerConfig.of(DefaultApplication.class));
        for (String path : new String[] { "/%2e%2e/private-config.txt", "/%252e%252e/private-config.txt", "/%5c../private-config.txt", "/.env", "/app.js.map" }) {
            HttpURLConnection connection = request(path, "GET");
            try {
                assertTrue(path, connection.getResponseCode() == 400 || connection.getResponseCode() == 404);
            } finally {
                connection.disconnect();
            }
        }
    }
}
