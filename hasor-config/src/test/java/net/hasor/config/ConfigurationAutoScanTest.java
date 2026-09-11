/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.List;
import javax.servlet.ServletContext;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.autoscan.AutoScanConfig;
import net.hasor.config.autoscan.AutoScanController;
import net.hasor.config.autoscan.AutoScanService;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.binder.MappingDef;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ConfigurationAutoScanTest {
    @Test
    public void hasorShouldDiscoverConfigurationWithoutApplicationModule() {
        AutoScanConfig.MODULE_LOADS.set(0);
        AppContext context = Hasor.create().addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.autoscan").addSettings(Settings.DefaultNameSpace, "hasor.config.scanPackages", "example.ignored").addSettings(Settings.DefaultNameSpace, "hasor.config.autoScan", false).addSettings(Settings.DefaultNameSpace, "hasor.config.web.autoScan", false).build();

        assertEquals("module-loaded", context.getInstance(AutoScanService.class).value());
        assertEquals(1, AutoScanConfig.MODULE_LOADS.get());
        assertSame(context.getInstance(AutoScanConfig.class), context.getInstance(AutoScanConfig.class));
    }

    @Test
    public void hasorShouldDiscoverWebControllerWithoutApplicationModule() {
        ServletContext servletContext = PowerMockito.mock(ServletContext.class);
        PowerMockito.when(servletContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        PowerMockito.when(servletContext.getContextPath()).thenReturn("/");
        PowerMockito.when(servletContext.getEffectiveMajorVersion()).thenReturn(3);
        PowerMockito.when(servletContext.getVirtualServerName()).thenReturn("test");

        AppContext context = Hasor.create(servletContext).addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.autoscan").build();

        List<MappingDef> mappings = context.findBindingBean(MappingDef.class);
        assertEquals(1, mappings.size());
        assertEquals("/auto-scan", mappings.get(0).getMappingTo());
        assertEquals(AutoScanController.class, mappings.get(0).getTargetType().getBindType());
    }
}
