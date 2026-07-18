package net.hasor.config.autoscan;

import net.hasor.config.Bean;
import net.hasor.config.Configuration;
import net.hasor.core.ApiBinder;
import net.hasor.core.Inject;
import net.hasor.core.Module;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AutoScanConfig implements Module {
    public static final AtomicInteger MODULE_LOADS = new AtomicInteger();

    @Inject
    private AutoScanMarker marker;

    @Override
    public void loadModule(ApiBinder apiBinder) {
        MODULE_LOADS.incrementAndGet();
        apiBinder.bindType(AutoScanMarker.class).toInstance(new AutoScanMarker("module-loaded"));
    }

    @Bean
    public AutoScanService autoScanService() {
        return new AutoScanService(this.marker.getValue());
    }
}
