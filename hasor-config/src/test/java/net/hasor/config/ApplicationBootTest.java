package net.hasor.config;

import net.hasor.config.application.SampleApplication;
import net.hasor.config.application.child.ScannedService;
import net.hasor.core.AppContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationBootTest {
    @Test
    public void applicationShouldScanItsOwnPackageAndChildren() {
        AppContext context = ApplicationBoot.run(SampleApplication.class);

        assertEquals("application-package", context.getInstance(ScannedService.class).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void applicationShouldRequireConfigurationPrimarySource() {
        ApplicationBoot.create(ScannedService.class);
    }
}
