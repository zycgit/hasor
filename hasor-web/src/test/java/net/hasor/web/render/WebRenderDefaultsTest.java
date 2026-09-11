/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebRenderDefaultsTest extends AbstractTest {
    public static class Action {
        @Get
        public String get() {
            return "hello";
        }
    }

    public static class CustomText implements RenderEngine {
        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            writer.write("custom");
        }
    }

    private AppContext context(Hasor builder) {
        return builder.build((WebModule) binder -> binder.mappingTo("/return").with(Action.class));
    }

    @Test
    public void webAloneLoadsDefaultsAndRendersText() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")))) {
            assertEquals("json", app.getSettings().getString("hasor.render.defaults.objectEngine"));
            assertEquals("text", app.getSettings().getString("hasor.render.defaults.stringEngine"));
            Set<String> types = new HashSet<>();
            assertEquals("hello", mockAndCallHttp("get", "http://localhost/return", app, types, null));
            assertTrue(types.contains("text/plain"));
        }
    }

    @Test
    public void applicationXmlOverridesMode() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")).mainSettingWith("/render-defaults-override.xml"))) {
            assertEquals("\"hello\"", mockAndCallHttp("GET", "http://localhost/return", app));
        }
    }

    @Test
    public void stringCanSelectJson() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")).addSettings(Settings.DefaultNameSpace, "hasor.render.defaults.stringEngine", "json"))) {
            assertEquals("\"hello\"", mockAndCallHttp("get", "http://localhost/return", app));
        }
    }

    @Test
    public void disabledDefaultsDoNotRenderReturnValue() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")).addSettings(Settings.DefaultNameSpace, "hasor.render.defaults.stringEngine", "none"))) {
            assertEquals("", mockAndCallHttp("get", "http://localhost/return", app));
        }
    }

    @Test
    public void noneLeavesResponseUntouched() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")).addSettings(Settings.DefaultNameSpace, "hasor.render.defaults.stringEngine", "none"))) {
            javax.servlet.http.HttpServletResponse response = org.powermock.api.mockito.PowerMockito.mock(javax.servlet.http.HttpServletResponse.class);
            net.hasor.web.invoker.InvokerContext invokers = new net.hasor.web.invoker.InvokerContext();
            invokers.initContext(app, new net.hasor.web.binder.OneConfig("", () -> app));
            invokers.genCaller(mockRequest("get", new java.net.URL("http://localhost/return")), response).invoke(null).get();
            org.mockito.Mockito.verify(response, org.mockito.Mockito.never()).setContentType(org.mockito.ArgumentMatchers.anyString());
            org.mockito.Mockito.verify(response, org.mockito.Mockito.never()).setCharacterEncoding(org.mockito.ArgumentMatchers.anyString());
            org.mockito.Mockito.verify(response, org.mockito.Mockito.never()).setContentLength(org.mockito.ArgumentMatchers.anyInt());
            org.mockito.Mockito.verify(response, org.mockito.Mockito.never()).getOutputStream();
            org.mockito.Mockito.verify(response, org.mockito.Mockito.never()).getWriter();
        }
    }

    @Test
    public void engineImplementationCanBeConfigured() throws Throwable {
        try (AppContext app = context(Hasor.create(servlet25("/")).mainSettingWith("/render-engines-override.xml"))) {
            assertEquals("custom", mockAndCallHttp("get", "http://localhost/return", app));
        }
    }

    @Test
    public void explicitRegistrationWinsWithoutLoadingReplacedClass() throws Throwable {
        try (AppContext app = Hasor.create(servlet25("/")).mainSettingWith("/render-engines-invalid.xml").build((WebModule) binder -> {
            binder.mappingTo("/return").with(Action.class);
            binder.addRender("text").toProvider(CustomText::new);
        })) {
            assertEquals("custom", mockAndCallHttp("get", "http://localhost/return", app));
        }
    }
}
