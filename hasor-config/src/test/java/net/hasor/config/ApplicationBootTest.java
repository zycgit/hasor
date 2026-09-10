package net.hasor.config;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import net.hasor.config.application.SampleApplication;
import net.hasor.config.application.child.ScannedService;
import net.hasor.core.AppContext;

public class ApplicationBootTest {
    @Test
    public void applicationShouldUseConfiguredCoreScanScope() {
        AppContext context = ApplicationBoot.create(SampleApplication.class).addSettings(net.hasor.cobble.setting.Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.application").build();

        assertEquals("application-package", context.getInstance(ScannedService.class).getValue());
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
