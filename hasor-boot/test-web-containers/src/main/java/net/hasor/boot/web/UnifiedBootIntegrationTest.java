/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.hasor.boot.Boot;
import net.hasor.boot.BootApplication;
import net.hasor.boot.fixtures.defaults.DefaultApplication;
import org.junit.Test;
import static org.junit.Assert.*;

/** 三种容器均通过统一 Boot 入口启动。 */
public abstract class UnifiedBootIntegrationTest extends ContainerIntegrationTest {
    public static class FailingApplication implements net.hasor.core.Module {
        static WebServer captured;

        public void loadModule(net.hasor.core.ApiBinder binder) {
        }

        public void onStart(net.hasor.core.AppContext context) {
            captured = context.getInstance(WebServer.class);
            throw new IllegalStateException("boot-start-failure");
        }
    }

    @Test
    public void failedApplicationStopsWebServer() throws Exception {
        FailingApplication.captured = null;
        try {
            new Boot().sources(FailingApplication.class).property("hasor.boot.web.connectors.http.port", 0).property("hasor.loadPackages", "example.empty").start();
            fail("Startup must fail.");
        } catch (Exception expected) {
            assertNotNull(FailingApplication.captured);
            assertFalse(FailingApplication.captured.isStart());
        }
    }

    @Test
    public void disabledHttpDoesNotBindConfiguredPort() throws Exception {
        try (java.net.ServerSocket occupied = new java.net.ServerSocket(0);
                BootApplication application = new Boot()
                        .property("hasor.boot.web.connectors.http.enabled", false)
                        .property("hasor.boot.web.connectors.http.port", occupied.getLocalPort())
                        .property("hasor.loadPackages", "example.empty").start()) {
            WebServer server = application.getAppContext().getInstance(WebServer.class);
            assertTrue(application.getAppContext().isStart());
            assertTrue(server.isStart());
            assertEquals(-1, server.getPort());
        }
    }

    @Test
    public void customServerContextPathIsUsed() throws Exception {
        try (BootApplication application = new Boot()
                .property("hasor.boot.web.server.contextPath", "/console")
                .property("hasor.boot.web.connectors.http.host", "127.0.0.1")
                .property("hasor.boot.web.connectors.http.port", 0)
                .property("hasor.loadPackages", "example.empty").start()) {
            WebServer server = application.getAppContext().getInstance(WebServer.class);
            assertEquals("/console", server.getContextPath());
            HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + server.getPort() + "/console/health").openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            try {
                assertEquals(200, connection.getResponseCode());
            } finally {
                connection.disconnect();
            }
        }
    }

    @Test
    public void unifiedEntryStartsWebAndClosesIt() throws Exception {
        WebServer server;
        BootApplication application = new Boot().sources(DefaultApplication.class).property("hasor.boot.web.connectors.http.port", 0).property("hasor.loadPackages", "net.hasor.boot.fixtures.defaults").start();
        try {
            server = application.getAppContext().getInstance(WebServer.class);
            assertTrue(server.isStart());
            HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + server.getPort() + "/hello").openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            try {
                assertEquals(200, connection.getResponseCode());
                assertEquals("你好", new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } finally {
                connection.disconnect();
            }
        } finally {
            application.close();
        }
        assertFalse(server.isStart());
        assertFalse(application.getAppContext().isStart());
    }
}
