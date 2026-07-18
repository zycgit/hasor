package net.hasor.config.application.child;

import net.hasor.config.Bean;
import net.hasor.config.Configuration;

@Configuration
public class ChildConfiguration {
    @Bean
    public ScannedService scannedService() {
        return new ScannedService("application-package");
    }
}
