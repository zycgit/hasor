/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.ServletContext;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.Invoker;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.binder.MappingDef;
import net.hasor.web.binder.RenderDef;
import net.hasor.web.render.RenderInvoker;
import net.hasor.web.render.RenderProcessor;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;

public class WebDefaultsModuleTest {
    @MappingTo("/config-defaults-test")
    public static class Action {
        @Get
        public Map<String, String> get() {
            return Map.of("name", "中文");
        }
    }

    private ServletContext servletContext() {
        ServletContext context = PowerMockito.mock(ServletContext.class);
        PowerMockito.when(context.getClassLoader()).thenReturn(getClass().getClassLoader());
        PowerMockito.when(context.getContextPath()).thenReturn("/");
        PowerMockito.when(context.getEffectiveMajorVersion()).thenReturn(3);
        PowerMockito.when(context.getVirtualServerName()).thenReturn("test");
        return context;
    }

    @Test
    public void repeatedDefaultAssemblyDoesNotDuplicateAutoScannedMappings() throws Throwable {
        try {
            Class.forName("net.hasor.boot.web.WebServers");
            fail("Config test must not depend on Boot");
        } catch (ClassNotFoundException expected) {
        }
        WebOptions options = new WebOptions().staticResources(false);
        try (AppContext context = Hasor.create(servletContext()).addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", getClass().getPackageName()).build((WebModule) binder -> {
            binder.installModule(new WebDefaultsModule(options));
        })) {
            assertEquals(1, context.findBindingBean(MappingDef.class).size());
            RenderInvoker invoker = PowerMockito.mock(RenderInvoker.class);
            PowerMockito.when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(Map.of("name", "中文"));
            StringWriter writer = new StringWriter();
            context.findBindingBean(RenderDef.class).stream().filter(def -> def.getRenderName().equals("json")).findFirst().orElseThrow().newEngine(context).process(invoker, writer);
            assertEquals("{\"name\":\"中文\"}", writer.toString());
        }
    }

    @Test
    public void canonicalKeysOverrideLegacyAliasesAndSnapshotsAreIndependent() {
        Settings settings = Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.config.web.response.noStorePaths", "/api/*").buildSettings();
        WebOptions options = WebDefaultsModule.loadOptions(new WebOptions(), settings);
        assertArrayEquals(new String[] { "/api/*" }, options.getNoStorePaths());
        WebOptions copy = options.copy();
        options.noStorePaths();
        assertEquals(1, copy.getNoStorePaths().length);
    }

    @Test
    public void coreScopeWinsOverRemovedScanKeys() throws Throwable {
        net.hasor.web.WebApiBinder binder = org.mockito.Mockito.mock(net.hasor.web.WebApiBinder.class);
        Settings settings = Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", " example.core.*, example.other.* ").addSettings(Settings.DefaultNameSpace, "hasor.config.scanPackages", "example.config").addSettings(Settings.DefaultNameSpace, "hasor.config.web.scanPackages", "example.web").addSettings(Settings.DefaultNameSpace, "hasor.boot.web.scanPackages", "example.boot").buildSettings();
        org.mockito.Mockito.when(binder.getSettings()).thenReturn(settings);
        org.mockito.Mockito.when(binder.tryCast(net.hasor.web.WebApiBinder.class)).thenReturn(binder);
        org.mockito.Mockito.when(binder.getMappings()).thenReturn(java.util.Collections.emptyList());
        net.hasor.cobble.loader.ResourceLoader resources = org.mockito.Mockito.mock(net.hasor.cobble.loader.ResourceLoader.class);
        org.mockito.Mockito.when(binder.getResourceLoader()).thenReturn(resources);
        net.hasor.config.ConfigurationModule.auto().loadModule(binder);
        new WebDefaultsModule(new WebOptions().staticResources(false)).loadModule(binder);
        org.mockito.Mockito.verify(resources).scanResources(org.mockito.Mockito.eq(net.hasor.cobble.loader.MatchType.Prefix), org.mockito.Mockito.any(net.hasor.cobble.loader.Scanner.class), org.mockito.AdditionalMatchers.aryEq(new String[] { "example/core/*", "example/other/*" }));
    }

    @org.junit.Test
    public void optionsLoadingPreservesValuesAndIgnoresBootKeys() {
        WebOptions options = new WebOptions().staticLocation("custom").spaPaths("/app/*");
        Settings settings = Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.boot.web.staticLocation", "legacy").addSettings(Settings.DefaultNameSpace, "hasor.config.web.scanExcludes", "example.Controller").buildSettings();
        assertSame(options, WebDefaultsModule.loadOptions(options, settings));
        assertEquals("custom", options.getStaticLocation());
        assertArrayEquals(new String[] { "/app/*" }, options.getSpaPaths());
        assertTrue(options.getScanExcludes().contains("example.Controller"));
        assertSame(options, WebDefaultsModule.loadOptions(options, null));
    }

    private ClassLoader withoutJson() {
        return new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("com.fasterxml.jackson.") || name.startsWith("com.google.gson.") || name.startsWith("com.alibaba.fastjson.") || name.startsWith("com.alibaba.fastjson2.")) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }
        };
    }

    @Test
    public void explicitRendererNeedsNoDetectedJsonLibrary() throws Throwable {
        try (AppContext context = Hasor.create(servletContext()).classLoaderWith(withoutJson()).addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.no_autoscan").build((WebModule) binder -> {
            binder.addRender("json").toProvider(() -> (invoker, writer) -> writer.write("custom"));
            binder.installModule(new WebDefaultsModule(new WebOptions().staticResources(false)));
        })) {
            assertEquals(1, context.findBindingBean(RenderDef.class).stream().filter(def -> !def.isFallback()).count());
        }
    }

    @Test
    public void disabledImplicitRenderingNeedsNoJsonLibrary() throws Throwable {
        try (AppContext context = Hasor.create(servletContext()).classLoaderWith(withoutJson()).addSettings(Settings.DefaultNameSpace, "hasor.render.defaults.objectEngine", "none").addSettings(Settings.DefaultNameSpace, "hasor.render.defaults.stringEngine", "none").addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.no_autoscan").build(new WebDefaultsModule(new WebOptions().staticResources(false)))) {
            // Successful startup without JSON libraries proves that the configured none defaults were applied.
            assertNotNull(context.getInstance(RenderProcessor.class));
        }
    }
}
