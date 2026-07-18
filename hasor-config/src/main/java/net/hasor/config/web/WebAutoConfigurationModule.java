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
package net.hasor.config.web;

import java.util.Arrays;
import java.util.Set;
import net.hasor.config.AutoConfigurationModule;
import net.hasor.core.DimModule;
import net.hasor.core.TypeSupplier;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.annotation.MappingTo;

/** Adds Web annotation scanning when hasor-web contributes a {@link WebApiBinder}. */
@DimModule
public class WebAutoConfigurationModule implements WebModule {
    public static final String AUTO_SCAN_ENABLED = "hasor.config.web.autoScan";
    public static final String SCAN_PACKAGES     = "hasor.config.web.scanPackages";

    @Override
    public void loadModule(WebApiBinder webBinder) throws Throwable {
        if (!webBinder.getSettings().getBoolean(AUTO_SCAN_ENABLED, true)) {
            return;
        }

        String[] scanPackages = cleanPackages(webBinder.getSettings().getStringArray(SCAN_PACKAGES));
        if (scanPackages.length == 0) {
            scanPackages = cleanPackages(webBinder.getSettings().getStringArray(AutoConfigurationModule.SCAN_PACKAGES));
        }
        if (scanPackages.length == 0) {
            scanPackages = cleanPackages(webBinder.getSettings().getStringArray("hasor.loadPackages"));
        }
        if (scanPackages.length == 0) {
            throw new IllegalStateException("Automatic Web configuration requires bounded scan packages. Configure '" + SCAN_PACKAGES + "'.");
        }

        Set<Class<?>> mappingTypes = webBinder.findClass(MappingTo.class, scanPackages);
        TypeSupplier containerTypes = new TypeSupplier() {
            @Override
            public <T> T get(Class<? extends T> targetType) {
                return webBinder.getProvider((Class<T>) targetType).get();
            }
        };
        webBinder.loadMappingTo(mappingTypes, type -> true, containerTypes);
    }

    private String[] cleanPackages(String[] scanPackages) {
        if (scanPackages == null) {
            return new String[0];
        }
        return Arrays.stream(scanPackages).filter(value -> value != null && !value.isBlank()).toArray(String[]::new);
    }
}
