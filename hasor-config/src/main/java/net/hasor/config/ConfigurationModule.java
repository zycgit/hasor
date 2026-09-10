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
import java.util.*;

import net.hasor.config.scanner.ConfigurationProcessor;
import net.hasor.config.scanner.MappingProcessor;
import net.hasor.config.scanner.Scanner;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import net.hasor.web.WebApiBinder;

/** 统一组织配置类和 Web 路由的扫描装配。 */
public class ConfigurationModule implements Module {
    private static final ConfigurationProcessor CONFIGURATION_PROCESSOR = new ConfigurationProcessor();
    private static final Scanner                SCANNER                 = createScanner();
    private final Set<Class<?>>                 configurationTypes;

    private static Scanner createScanner() {
        try {
            Class<?> webType = WebApiBinder.class;
        } catch (NoClassDefFoundError ignored) {
            return new Scanner(CONFIGURATION_PROCESSOR);
        }

        return new Scanner(CONFIGURATION_PROCESSOR, new MappingProcessor());
    }

    /** 按 Core 配置自动扫描，供 hconfig 实例化。 */
    public ConfigurationModule() {
        this.configurationTypes = null;
    }

    /** 供子类声明需要加载的配置类。 */
    protected ConfigurationModule(Class<?>... configurationTypes) {
        this.configurationTypes = new LinkedHashSet<>(Arrays.asList(Objects.requireNonNull(configurationTypes, "configurationTypes is null.")));
    }

    public static ConfigurationModule auto() {
        return new ConfigurationModule();
    }

    /** 只加载指定配置类，不执行自动扫描。 */
    public static ConfigurationModule of(Class<?>... configurationTypes) {
        return new ConfigurationModule(configurationTypes);
    }

    @Override
    public void loadModule(ApiBinder binder) throws Throwable {
        if (this.configurationTypes != null) {
            CONFIGURATION_PROCESSOR.process(binder, new ArrayList<>(this.configurationTypes));
            return;
        }

        String[] packages = Arrays.stream(binder.getSettings().getString("hasor.loadPackages", "").split(",")).map(String::trim).filter(value -> {
            return !value.isEmpty();
        }).toArray(String[]::new);
        if (packages.length == 0) {
            return;
        }

        SCANNER.scan(binder, packages);
    }
}
