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
package net.hasor.config;

import java.util.Arrays;
import java.util.Set;
import net.hasor.config.core.ConfigurationModule;
import net.hasor.core.ApiBinder;
import net.hasor.core.DimModule;
import net.hasor.core.Module;

/** Internal bootstrap that enables annotation-driven applications without an application {@link Module}. */
public class AutoConfigurationModule implements Module {
    public static final  String AUTO_SCAN_ENABLED         = "hasor.config.autoScan";
    public static final  String SCAN_PACKAGES             = "hasor.config.scanPackages";
    private static final String HASOR_LOAD_PACKAGES       = "hasor.loadPackages";
    private static final String CONFIG_EXTENSIONS_PACKAGE = "net.hasor.config.web";

    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        if (!apiBinder.getSettings().getBoolean(AUTO_SCAN_ENABLED, true)) {
            return;
        }

        String[] scanPackages = apiBinder.getSettings().getStringArray(SCAN_PACKAGES);
        scanPackages = cleanPackages(scanPackages);
        if (scanPackages.length == 0) {
            scanPackages = apiBinder.getSettings().getStringArray(HASOR_LOAD_PACKAGES);
            scanPackages = cleanPackages(scanPackages);
        }
        if (scanPackages.length == 0) {
            return;
        }

        Set<Class<?>> configurationTypes = apiBinder.findClass(Configuration.class, scanPackages);
        if (!configurationTypes.isEmpty()) {
            new ConfigurationModule(configurationTypes.toArray(new Class<?>[0])).loadModule(apiBinder);
        }

        Set<Class<?>> extensionModules = apiBinder.findClass(DimModule.class, CONFIG_EXTENSIONS_PACKAGE);
        apiBinder.loadModule(extensionModules);
    }

    private String[] cleanPackages(String[] scanPackages) {
        if (scanPackages == null) {
            return new String[0];
        } else {
            return Arrays.stream(scanPackages).filter(value -> {
                return value != null && !value.isBlank();
            }).toArray(String[]::new);
        }
    }
}
