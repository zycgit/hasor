/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import net.hasor.cobble.*;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.provider.Provider;
import net.hasor.cobble.setting.DefaultSettings;
import net.hasor.cobble.setting.Settings;
import net.hasor.cobble.setting.provider.StreamType;
import net.hasor.core.container.BeanContainer;
import net.hasor.core.container.TemplateAppContext;
import net.hasor.core.info.Arguments;

/**
 * Hasor 基础工具包。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-3
 */
public final class Hasor {
    private final static Logger                           logger               = LoggerFactory.getLogger(Hasor.class);
    public final static  String                           SchemaName           = "/META-INF/hasor.schemas";
    public               String                           mainSettings         = "hconfig.xml";
    private final        Object                           context;
    private final        List<Module>                     moduleList           = new ArrayList<>();
    private final        Set<Class<?>>                    primarySources       = new LinkedHashSet<>();
    private              ClassLoader                      loader;
    private              Arguments                        arguments            = new Arguments(null);
    private              boolean                          registerShutdownHook = true;
    private final        Map<String, Map<String, Object>> initSettingMap       = new HashMap<>();

    private Hasor(Object context) {
        this.context = context;
    }

    public Hasor mainSettingWith(String mainSettings) {
        this.mainSettings = mainSettings;
        return this;
    }

    public Hasor classLoaderWith(ClassLoader loader) {
        this.loader = loader;
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

    public Hasor bindArguments(String... args) {
        this.arguments = new Arguments(args);
        return this;
    }

    public Hasor registerShutdownHook(boolean registerShutdownHook) {
        this.registerShutdownHook = registerShutdownHook;
        return this;
    }

    public Hasor addPrimarySources(Class<?>... primarySources) {
        if (primarySources != null) {
            for (Class<?> primarySource : primarySources) {
                this.primarySources.add(Objects.requireNonNull(primarySource, "primarySource must not be null."));
            }
        }
        return this;
    }

    /** 用简易的方式创建{@link Settings}容器。 */
    public DefaultSettings buildSettings() {
        // .单独处理RUN_PATH
        String runPath = new File("").getAbsolutePath();
        System.setProperty("RUN_PATH", runPath);
        //
        try {
            DefaultSettings mainSettings = new DefaultSettings();
            loadPluginSettings(mainSettings);
            loadSettings(mainSettings, this.mainSettings);

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

    private static void loadPluginSettings(DefaultSettings configSetting) throws IOException {
        // 装载所有 xxx-hconfig.xml
        Map<String, URL> toLoading = new HashMap<>();
        List<URL> schemaUrlList = ResourcesUtils.getResources(SchemaName);
        for (URL schemaUrl : schemaUrlList) {
            InputStream schemaStream = ResourcesUtils.getResourceAsStream(schemaUrl);
            List<String> readLines = IOUtils.readLines(schemaStream, StandardCharsets.UTF_8);
            if (readLines.isEmpty()) {
                logger.warn("found nothing , " + schemaUrl);
                continue;
            }
            for (String schema : readLines) {
                toLoading.put(schema, schemaUrl);
            }
        }

        for (Map.Entry<String, URL> entry : toLoading.entrySet()) {
            String resource = entry.getKey();
            URL schemaUrl = entry.getValue();
            if (loadSettings(configSetting, resource)) {
                logger.info("config loaded '" + resource + "' from '" + schemaUrl + "'");
            } else {
                logger.info("config cannot be read '" + resource + "' from '" + schemaUrl + "'");
            }
        }
    }

    private static boolean loadSettings(DefaultSettings configSetting, String resource) throws IOException {
        StreamType streamType = getStreamType(resource);
        if (streamType == null) {
            return false;
        }
        try (InputStream in = ResourcesUtils.getResourceAsStream(resource)) {
            configSetting.loadStream(in, streamType);
        }

        return true;
    }

    private static StreamType getStreamType(String schemaUrl) {
        if (schemaUrl == null) {
            return null;
        }
        String lowerCase = schemaUrl.toLowerCase();
        if (lowerCase.endsWith(".xml")) {
            return StreamType.Xml;
        } else if (lowerCase.endsWith(".yaml") || lowerCase.endsWith(".yml")) {
            return StreamType.Yaml;
        } else if (lowerCase.endsWith(".properties")) {
            return StreamType.Properties;
        } else {
            return null;
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

            ClassLoader classLoader = this.loader == null ? Thread.currentThread().getContextClassLoader() : this.loader;
            BeanContainer container = new BeanContainer(settings, classLoader, new ClassPathResourceLoader(classLoader), this.context);
            AppContext appContext = new TemplateAppContext() {
                @Override
                protected BeanContainer getContainer() {
                    return container;
                }
            };

            List<Module> ms = new ArrayList<>(this.moduleList);
            this.addArgumentsModule(ms);
            this.addPrimarySourcesModule(ms);
            appContext.start(ms.toArray(new Module[0]));
            this.registerShutdownHook(appContext);
            return appContext;
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private void registerShutdownHook(AppContext appContext) {
        if (!this.registerShutdownHook) {
            return;
        }
        AutoCloseable shutdownHook = SystemUtils.registerShutdownHook(() -> {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
                if (appContext.isStart()) {
                    appContext.shutdown();
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
            return null;
        });
        HasorUtils.pushShutdownListener(appContext.getEventContext(), (event, eventData) -> {
            try {
                shutdownHook.close();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    private void addArgumentsModule(List<Module> modules) {
        modules.add(0, a -> {
            a.bindType(Arguments.class).toInstance(this.arguments);
            a.bindType(String[].class).nameWith(Arguments.MAIN_ARGS).toInstance(this.arguments.args());
        });
    }

    private void addPrimarySourcesModule(List<Module> modules) {
        modules.add(a -> {
            for (Class<?> primarySource : this.primarySources) {
                BindInfo<?> registered = a.findBindingRegister("", primarySource);
                if (registered != null && Boolean.TRUE.equals(registered.getMetaData(Module.MODULE_INSTALLED))) {
                    continue;
                }

                Provider<Object> primaryProvider = ((Provider<Object>) () -> ClassUtils.newInstance(primarySource)).asSingle();
                BindInfo<?> info = a.bindType((Class) primarySource).toProvider(primaryProvider).toInfo();

                a.lazyLoad(app -> {
                    Object primaryObj = app.getInstance(info);
                    app.justInject(primaryObj, primarySource);
                });

                if (Module.class.isAssignableFrom(primarySource)) {
                    a.installModule(((Module) primaryProvider.get()));
                }
            }
        });
    }

    /** 用Builder的方式创建{@link AppContext}容器。 */
    public static Hasor create() {
        return new Hasor(null);
    }

    /** 用Builder的方式创建{@link AppContext}容器。 */
    public static Hasor create(Object context) {
        return new Hasor(context);
    }

    /** 用简易的方式创建{@link AppContext}容器。 */
    public static AppContext run(String[] args, Class<?>... primarySources) {
        return Hasor.create().bindArguments(args).addPrimarySources(primarySources).build();
    }
}
