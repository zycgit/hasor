package net.hasor.boot.web;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class WebServerProviderSelectionTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    public static class FirstProvider extends WebServerConfigTest.MockWebServerProvider {
        @Override
        public String name() { return "first"; }
    }

    public static class SecondProvider extends WebServerConfigTest.MockWebServerProvider {
        @Override
        public String name() { return "second"; }
    }

    private WebServer create(Class<?>... providers) throws Exception {
        java.nio.file.Path service = temporary.newFile().toPath();
        StringBuilder content = new StringBuilder();
        for (Class<?> provider : providers) {
            content.append(provider.getName()).append('\n');
        }
        Files.writeString(service, content, StandardCharsets.UTF_8);
        URL resource = service.toUri().toURL();
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new ClassLoader(previous) {
            @Override
            public Enumeration<URL> getResources(String path) throws java.io.IOException {
                if (path.equals("META-INF/services/" + WebServerProvider.class.getName())) {
                    return Collections.enumeration(Collections.singletonList(resource));
                }
                return super.getResources(path);
            }
        };
        try {
            Thread.currentThread().setContextClassLoader(loader);
            return WebServers.create(new WebServerConfig());
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Test
    public void singleProviderIsSelectedAutomatically() throws Exception {
        assertTrue(create(FirstProvider.class) instanceof WebServerConfigTest.MockWebServer);
    }

    @Test
    public void multipleProvidersFail() throws Exception {
        try {
            create(FirstProvider.class, SecondProvider.class);
            fail("Expected multiple providers to fail");
        } catch (IllegalStateException error) {
            assertTrue(error.getMessage().contains("first"));
            assertTrue(error.getMessage().contains("second"));
            assertTrue(error.getMessage().contains("Keep only one"));
        }
    }

    @Test
    public void missingProviderFailsClearly() throws Exception {
        try {
            create();
            fail("Expected missing provider to fail");
        } catch (IllegalStateException error) {
            assertTrue(error.getMessage().contains("No embedded WebServerProvider"));
        }
    }
}
