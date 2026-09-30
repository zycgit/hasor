/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.resource;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.loader.providers.PathResourceLoader;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.CacheControl;
import net.hasor.web.DelegatingServletOutputStream;
import net.hasor.web.WebModule;
import net.hasor.web.binder.OneConfig;
import net.hasor.web.invoker.InvokerContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ResourceCacheControlTest extends AbstractTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void typedPolicyReachesGetHeadAndConditionalResponses() throws Throwable {
        Path root = this.directory();
        long modified = Files.getLastModifiedTime(root.resolve("app.txt")).toMillis();
        CacheControl policy = CacheControl.maxAge(Duration.ofHours(1)).cachePublic().mustRevalidate();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile())).cacheControl(policy);
        })) {
            for (String method : new String[] { "GET", "HEAD" }) {
                HttpServletResponse response = this.request(app, method, -1);
                verify(response).setHeader("Cache-Control", "max-age=3600, public, must-revalidate");
                verify(response).setContentLengthLong(5);
                if ("HEAD".equals(method)) {
                    verify(response, never()).getOutputStream();
                }

                HttpServletResponse cached = this.request(app, method, modified);
                verify(cached).setStatus(304);
                verify(cached).setHeader("Cache-Control", "max-age=3600, public, must-revalidate");
                verify(cached, never()).getOutputStream();
            }
        }
    }

    private Path directory() throws Exception {
        Path root = this.temporary.newFolder().toPath();
        Files.writeString(root.resolve("app.txt"), "asset");
        return root;
    }

    private HttpServletResponse request(AppContext app, String method, long modifiedSince) throws Throwable {
        HttpServletRequest request = this.mockRequest(method, new URL("http://localhost/assets/app.txt"));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(modifiedSince);
        HttpServletResponse response = mock(HttpServletResponse.class);
        this.mockRenderResponse(response);
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
        InvokerContext context = new InvokerContext();
        context.initContext(app, new OneConfig("", () -> app));
        context.genCaller(request, response).invoke(null).get();
        return response;
    }

    @Test
    public void emptyPolicyDoesNotWriteACacheControlHeader() throws Throwable {
        Path root = this.directory();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile())).cacheControl(CacheControl.empty());
        })) {
            HttpServletResponse response = this.request(app, "GET", -1);
            verify(response, never()).setHeader(eq("Cache-Control"), any());
            verify(response).setContentLengthLong(5);
        }
    }

    @Test
    public void omittedPolicyStillRequiresRevalidation() throws Throwable {
        Path root = this.directory();
        try (AppContext app = Hasor.create(this.servlet30("/")).build((WebModule) binder -> {
            binder.addResource("/assets/**", new PathResourceLoader(root.toFile()));
        })) {
            verify(this.request(app, "GET", -1)).setHeader("Cache-Control", "no-cache");
        }
    }
}
