/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import net.hasor.config.application.SampleApplication;
import net.hasor.config.application.child.ScannedService;
import net.hasor.core.AppContext;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ApplicationBootTest {
    @Test
    public void applicationShouldUseConfiguredCoreScanScope() {
        AppContext context = ApplicationBoot.create(SampleApplication.class).addSettings(net.hasor.cobble.setting.Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.application").build();

        assertEquals("application-package", context.getInstance(ScannedService.class).value());
    }

    @Test
    public void applicationDoesNotInferScanScope() {
        assertEquals(net.hasor.core.Hasor.create().buildSettings().getString("hasor.loadPackages"), ApplicationBoot.create(SampleApplication.class).buildSettings().getString("hasor.loadPackages"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void applicationShouldRequireConfigurationPrimarySource() {
        ApplicationBoot.create(ScannedService.class);
    }
}
