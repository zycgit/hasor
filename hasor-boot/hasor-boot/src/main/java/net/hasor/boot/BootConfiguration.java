/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.Configuration;
import net.hasor.config.ConfigurationModule;
import net.hasor.core.Hasor;

/** 单次启动的配置快照，不区分应用类型。 */
public final class BootConfiguration {
    private final String[]            arguments;
    private final Class<?>[]          sources;
    private final String              hconfigFile;
    private final ClassLoader         classLoader;
    private final Map<String, Object> properties;
    private final Settings            settings;

    BootConfiguration(String[] arguments, Class<?>[] sources, String hconfigFile, ClassLoader classLoader, Map<String, Object> properties) {
        this.arguments = arguments.clone();
        this.sources = sources.clone();
        this.hconfigFile = hconfigFile;
        this.classLoader = classLoader;
        this.properties = new LinkedHashMap<>(properties);
        this.settings = newHasor(null).buildSettings();
    }

    public Settings getSettings() {
        return settings;
    }

    public String[] getArguments() {
        return arguments.clone();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    Hasor newHasor(Object environment) {
        Hasor hasor = Hasor.create(environment).classLoaderWith(classLoader).mainSettingWith(hconfigFile).bindArguments(arguments).registerShutdownHook(false);
        properties.forEach((key, value) -> hasor.addSettings(Settings.DefaultNameSpace, key, value));
        return hasor;
    }

    void configureSources(Hasor hasor) {
        for (Class<?> source : sources) {
            if (source.isAnnotationPresent(Configuration.class)) {
                hasor.addModules(ConfigurationModule.of(source));
            } else {
                hasor.addPrimarySources(source);
            }
        }
    }
}
