/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.boot.web;
import static org.junit.Assert.*;
import org.junit.Test;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.Hasor;
import net.hasor.core.Module;

public class WebServerConfigTest {
    @Test
    public void responseSettingsAreCopiedAndOverridden() {
        WebServerConfig defaults = new WebServerConfig();
        assertEquals(0, defaults.getNoStorePaths().length);
        WebServerConfig source = WebServerConfig.of(Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.boot.web.response.noStorePaths", "/api/*").buildSettings(), StartModule.class);
        WebServerConfig copy = source.copy();
        source.noStorePaths();
        String[] paths = copy.getNoStorePaths();
        paths[0] = "/changed";
        assertArrayEquals(new String[] { "/api/*" }, copy.getNoStorePaths());
    }

    @Test
    public void bootDefaultsAreCopiedWithoutSharingMutableCollections() {
        WebServerConfig original = WebServerConfig.of(StartModule.class).spaPaths("/app/*").excludeScan(StartModule.class).staticLocation("public-ui").resourceExcludes("/rpc");
        WebServerConfig copy = original.copy();
        assertTrue(copy.isStaticResources());
        original.staticResources(false).spaPaths("/other/*");
        original.excludeScan(WebServerConfigTest.class);
        assertArrayEquals(new String[] { "/app/*" }, copy.getSpaPaths());
        assertArrayEquals(new String[] { "/rpc" }, copy.getResourceExcludes());
        assertEquals("public-ui", copy.getStaticLocation());
        assertEquals(1, copy.getScanExcludes().size());
        assertTrue(copy.isStaticResources());
    }

    @Test
    public void bootSettingsCanBeOverriddenProgrammatically() {
        WebServerConfig config = WebServerConfig.of(Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.boot.web.staticResources", false).addSettings(Settings.DefaultNameSpace, "hasor.boot.web.staticLocation", "public-ui").addSettings(Settings.DefaultNameSpace, "hasor.boot.web.spaPaths", "/app/*").buildSettings(), StartModule.class);
        assertFalse(config.isStaticResources());
        assertArrayEquals(new String[] { "/app/*" }, config.getSpaPaths());
        assertEquals("public-ui", config.getStaticLocation());
    }

    @Test
    public void configNamespaceOverridesLegacyBootOptions() {
        Settings settings = Hasor.create()
                .addSettings(Settings.DefaultNameSpace, "hasor.boot.web.staticLocation", "legacy")
                .addSettings(Settings.DefaultNameSpace, "hasor.config.web.staticLocation", "current")
                .addSettings(Settings.DefaultNameSpace, "hasor.boot.web.scanExcludes", "example.Legacy")
                .addSettings(Settings.DefaultNameSpace, "hasor.config.web.scanExcludes", "example.Current")
                .buildSettings();
        WebServerConfig config = WebServerConfig.of(settings, StartModule.class);
        assertEquals("current", config.getStaticLocation());
        assertTrue(config.getScanExcludes().contains("example.Legacy"));
        assertTrue(config.getScanExcludes().contains("example.Current"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void refusesClasspathRootExposure() {
        new WebServerConfig().staticLocation("/");
    }

    @Test
    public void defaultSettings() {
        Settings settings = Hasor.create().buildSettings();
        // Assert resource discovery itself, not merely WebServerConfig's Java fallbacks.
        assertEquals("0.0.0.0", settings.getString("hasor.http.host"));
        assertEquals(Integer.valueOf(8080), settings.getInteger("hasor.http.port"));
        assertEquals("/", settings.getString("hasor.http.contextPath"));
        WebServerConfig config = WebServerConfig.of(settings, StartModule.class);
        assertEquals("0.0.0.0", config.getHost());
        assertEquals(8080, config.getPort());
        assertEquals("/", config.getContextPath());
        assertEquals("hasorFilter", config.getFilterName());
        assertEquals("/*", config.getFilterPattern());
        assertEquals(StartModule.class, config.getRootModule());
        assertNull(config.getServer());
    }

    @Test
    public void webHconfigClassReferences() throws Exception {
        Settings settings = Hasor.create().buildSettings();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assertEquals(System.getProperty("RUN_PATH") + "/temp/fragment", settings.getString("hasor.fileupload.cacheDirectory"));
        assertLoadable(classLoader, settings.getStringArray("hasor.modules.module"));
        assertLoadable(classLoader, settings.getStringArray("hasor.autoLoadSpi.spi"));
        assertLoadable(classLoader, settings.getNodeArray("hasor.apiBinderSet.binder"));
        assertLoadable(classLoader, settings.getNodeArray("hasor.invokerCreatorSet.invokerCreator"));
    }

    @Test
    public void overrideSettings() {
        WebServerConfig config = WebServerConfig.of(Hasor.create()//
                .addSettings(Settings.DefaultNameSpace, "hasor.http.server", "mock")//
                .addSettings(Settings.DefaultNameSpace, "hasor.http.host", "127.0.0.1")//
                .addSettings(Settings.DefaultNameSpace, "hasor.http.port", "18080")//
                .addSettings(Settings.DefaultNameSpace, "hasor.http.contextPath", "demo")//
                .buildSettings(), StartModule.class);

        assertEquals("mock", config.getServer());
        assertEquals("127.0.0.1", config.getHost());
        assertEquals(18080, config.getPort());
        assertEquals("/demo", config.getContextPath());
        assertEquals("hasorFilter", config.getFilterName());
        assertEquals("/*", config.getFilterPattern());
        assertNull(config.getHconfigFile());
    }

    @Test
    public void createByServerName() {
        WebServer server = WebServers.create(WebServerConfig.of(StartModule.class)//
                .server("mock")//
                .arguments("a", "b"));
        assertTrue(server instanceof MockWebServer);
        assertArrayEquals(new String[] { "a", "b" }, ((MockWebServer) server).getConfig().getArguments());
    }

    private static void assertLoadable(ClassLoader classLoader, String[] classNames) throws Exception {
        for (String className : classNames) {
            assertNotNull(classLoader.loadClass(className));
        }
    }

    private static void assertLoadable(ClassLoader classLoader, SettingNode[] nodes) throws Exception {
        for (SettingNode node : nodes) {
            assertNotNull(classLoader.loadClass(node.getValue()));
            assertNotNull(classLoader.loadClass(node.getSubValue("type")));
        }
    }

    public static class StartModule implements Module {
        @Override
        public void loadModule(ApiBinder apiBinder) {
            //
        }
    }

    public static class MockWebServer extends AbstractWebServer {
        public MockWebServer(WebServerConfig config) {
            super(config);
        }

        public WebServerConfig getConfig() {
            return this.config;
        }

        @Override
        public void start() {
            markStarting();
        }

        @Override
        public void stop() {
            markStopping();
        }
    }

    public static class MockWebServerProvider implements WebServerProvider {
        @Override
        public String name() {
            return "mock";
        }

        @Override
        public WebServer create(WebServerConfig config) {
            return new MockWebServer(config);
        }
    }
}
