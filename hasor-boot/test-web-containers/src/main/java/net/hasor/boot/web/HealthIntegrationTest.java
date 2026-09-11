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
import net.hasor.boot.web.health.HealthCheck;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import net.hasor.web.WebApiBinder;
import net.hasor.web.annotation.Get;
import org.junit.Test;
import static org.junit.Assert.*;

public abstract class HealthIntegrationTest extends ContainerIntegrationTest {
    private Boot builder() {
        return new Boot().property("hasor.boot.web.connectors.http.port", 0).property("hasor.loadPackages", "example.empty");
    }

    private String request(BootApplication application, String path, int status) throws Exception {
        WebServer server = application.getAppContext().getInstance(WebServer.class);
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + server.getPort() + path).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        try {
            assertEquals(status, connection.getResponseCode());
            if (status == 404)
                return "";
            if (status == 200 || status == 503) {
                assertEquals("no-store", connection.getHeaderField("Cache-Control"));
                assertTrue(connection.getContentType().startsWith("application/json"));
            }
            try (java.io.InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void defaultHealthIsUp() throws Exception {
        try (BootApplication app = builder().start()) {
            assertEquals("{\"status\":\"UP\"}", request(app, "/health", 200));
        }
    }

    @Test
    public void customAddressReplacesDefault() throws Exception {
        try (BootApplication app = builder().property("hasor.boot.web.health.path", "/internal/alive").start()) {
            request(app, "/health", 404);
            assertTrue(request(app, "/internal/alive", 200).contains("UP"));
        }
    }

    @Test
    public void endpointCanBeDisabled() throws Exception {
        try (BootApplication app = builder().property("hasor.boot.web.health.enabled", false).property("hasor.boot.web.health.path", "/internal/alive").start()) {
            request(app, "/health", 404);
            request(app, "/internal/alive", 404);
        }
    }

    @Test
    public void customChecksAreAggregated() throws Exception {
        try (BootApplication app = builder().sources(Checks.class).start()) {
            String body = request(app, "/health", 503);
            assertTrue(body.contains("\"database\":\"DOWN\""));
            assertTrue(body.contains("\"cache\":\"UP\""));
        }
    }

    @Test
    public void checkExceptionsDoNotLeakDetails() throws Exception {
        try (BootApplication app = builder().sources(Checks.class).property("test.health.throw", true).start()) {
            String body = request(app, "/health", 503);
            assertTrue(body.contains("\"database\":\"DOWN\""));
            assertFalse(body.contains("secret"));
        }
    }

    public static class ExistingEndpoint {
        @Get
        public String health() {
            return "business-health";
        }
    }

    public static class ExistingApplication implements net.hasor.web.WebModule {
        public void loadModule(WebApiBinder binder) {
            binder.mappingTo("/health").with(ExistingEndpoint.class);
        }
    }

    @Test
    public void existingBusinessEndpointWins() throws Exception {
        try (BootApplication app = builder().sources(ExistingApplication.class).start()) {
            WebServer server = app.getAppContext().getInstance(WebServer.class);
            HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + server.getPort() + "/health").openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            try {
                assertEquals(200, connection.getResponseCode());
                try (java.io.InputStream input = connection.getInputStream()) {
                    assertEquals("business-health", new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
            } finally {
                connection.disconnect();
            }
        }
    }

    @net.hasor.config.Configuration
    public static class BeanChecks {
        @net.hasor.config.Bean
        public HealthCheck database() {
            return () -> true;
        }

        @net.hasor.config.Bean("queue")
        public HealthCheck messageQueue() {
            return () -> false;
        }
    }

    @Test
    public void beanNamesIdentifyChecks() throws Exception {
        try (BootApplication app = builder().sources(BeanChecks.class).start()) {
            String body = request(app, "/health", 503);
            assertTrue(body.contains("\"database\":\"UP\""));
            assertTrue(body.contains("\"queue\":\"DOWN\""));
            assertFalse(body.contains("messageQueue"));
        }
    }

    public static class Checks implements Module {
        public void loadModule(ApiBinder binder) {
            boolean fail = binder.getSettings().getBoolean("test.health.throw", false);
            binder.bindType(HealthCheck.class).nameWith("database").toInstance(new HealthCheck() {
                public boolean check() {
                    if (fail)
                        throw new IllegalStateException("secret-connection");
                    return false;
                }
            });
            binder.bindType(HealthCheck.class).nameWith("cache").toInstance(new HealthCheck() {
                public boolean check() {
                    return true;
                }
            });
        }
    }
}
