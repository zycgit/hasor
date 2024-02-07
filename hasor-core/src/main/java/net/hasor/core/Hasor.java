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
package net.hasor.core;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.setting.DefaultSettings;
import net.hasor.cobble.setting.Settings;
import net.hasor.cobble.setting.provider.StreamType;
import net.hasor.core.container.BeanContainer;
import net.hasor.core.container.TemplateAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Hasor 基础工具包。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-3
 */
public final class Hasor {
    protected final static Logger logger     = LoggerFactory.getLogger(Hasor.class);
    public                 String SchemaName = "/META-INF/hasor.schemas";

    public        Object                           mainSettings           = "hconfig.xml";
    private final Object                           context;
    private       StreamType                       mainSettingsStreamType = null;
    private final List<Module>                     moduleList             = new ArrayList<>();
    private       ClassLoader                      loader;
    private final Map<String, Map<String, Object>> initSettingMap         = new HashMap<>();
    private final Map<String, String>              variableMap            = new HashMap<>();

    protected Hasor(Object context) {
        this.context = context;
    }

    public Hasor mainSettingWith(File mainSettings) {
        this.mainSettings = mainSettings;
        return this;
    }

    public Hasor mainSettingWith(URI mainSettings) {
        this.mainSettings = mainSettings;
        return this;
    }

    public Hasor mainSettingWith(URL mainSettings) {
        this.mainSettings = mainSettings;
        return this;
    }

    public Hasor mainSettingWith(String mainSettings) {
        this.mainSettings = mainSettings;
        return this;
    }

    public Hasor addSettings(String namespace, String key, Object value) {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("namespace or key is null.");
        }
        Map<String, Object> stringMap = this.initSettingMap.computeIfAbsent(namespace, k -> new HashMap<>());
        stringMap.put(key, value);
        return this;
    }

    /** 加载配置文件到默认配置空间。 */
    public Hasor loadSettings(Properties properties) {
        return loadSettings(Settings.DefaultNameSpace, properties);
    }

    /** 加载配置文件到指定配置空间。 */
    public Hasor loadSettings(String namespace, Properties properties) {
        if (properties != null) {
            for (Object key : properties.keySet()) {
                this.addSettings(namespace, key.toString(), properties.getProperty(key.toString()));
            }
        }
        return this;
    }

    public Hasor addModules(List<Module> moduleList) {
        if (moduleList != null) {
            this.moduleList.addAll(moduleList);
        }
        return this;
    }

    public Hasor addModules(Module... modules) {
        if (modules != null) {
            this.moduleList.addAll(Arrays.asList(modules));
        }
        return this;
    }

    public Hasor parentClassLoaderWith(ClassLoader loader) {
        this.loader = loader;
        return this;
    }

    /** 用简易的方式创建{@link Settings}容器。 */
    public Settings buildSettings() {
        // .单独处理RUN_PATH
        String runPath = new File("").getAbsolutePath();
        System.setProperty("RUN_PATH", runPath);
        //
        try {
            DefaultSettings mainSettings;
            if (this.mainSettings == null) {
                mainSettings = new DefaultSettings("hconfig.xml");
            } else if (this.mainSettings instanceof String) {
                if (StringUtils.isBlank(this.mainSettings.toString())) {
                    mainSettings = new DefaultSettings("hconfig.xml");
                } else {
                    mainSettings = new DefaultSettings((String) this.mainSettings);
                }
            } else if (this.mainSettings instanceof File) {
                mainSettings = new DefaultSettings((File) this.mainSettings);
            } else if (this.mainSettings instanceof URI) {
                mainSettings = new DefaultSettings((URI) this.mainSettings);
            } else if (this.mainSettings instanceof URL) {
                mainSettings = new DefaultSettings(((URL) this.mainSettings).toURI());
            } else {
                throw new UnsupportedOperationException();
            }
            //
            for (Map.Entry<String, Map<String, Object>> namespaceData : this.initSettingMap.entrySet()) {
                String namespaceKey = namespaceData.getKey();
                Map<String, Object> value = namespaceData.getValue();
                if (StringUtils.isBlank(namespaceKey) || value.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, Object> settingKV : value.entrySet()) {
                    mainSettings.setSetting(settingKV.getKey(), settingKV.getValue(), namespaceKey);
                }
            }
            return mainSettings;
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    /** 用简易的方式创建{@link AppContext}容器。 */
    public AppContext build(Module... modules) {
        if (modules != null) {
            this.addModules(modules);
        }
        //
        try {
            Settings settings = buildSettings();

            BeanContainer container = new BeanContainer(settings, this.loader, new ClassPathResourceLoader(this.loader), this.context);
            container.init();
            AppContext appContext = new TemplateAppContext() {
                @Override
                protected BeanContainer getContainer() {
                    return container;
                }
            };
            appContext.start(this.moduleList.toArray(new Module[0]));
            return appContext;
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    /** 用Builder的方式创建{@link AppContext}容器。 */
    public static Hasor create() {
        return new Hasor(null);
    }

    /** 用Builder的方式创建{@link AppContext}容器。 */
    public static Hasor create(Object context) {
        return new Hasor(context);
    }
}
