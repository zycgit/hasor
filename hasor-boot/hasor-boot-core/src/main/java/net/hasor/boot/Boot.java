/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
import java.util.*;
import net.hasor.cobble.SystemUtils;
import net.hasor.core.Hasor;

/** 统一应用启动入口，具体运行环境由扩展提供。 */
public final class Boot {
    private       String[]            arguments   = new String[0];
    private       Class<?>[]          sources     = new Class<?>[0];
    private       String              hconfigFile = "hconfig.xml";
    private final Map<String, Object> properties  = new LinkedHashMap<>();

    public static BootApplication run(String[] args, Class<?>... sources) throws Exception {
        return new Boot().arguments(args).sources(sources).start();
    }

    public Boot arguments(String... arguments) {
        this.arguments = Objects.requireNonNull(arguments, "arguments").clone();
        return this;
    }

    public Boot sources(Class<?>... sources) {
        this.sources = Objects.requireNonNull(sources, "sources").clone();
        for (Class<?> source : this.sources)
            Objects.requireNonNull(source, "source");
        return this;
    }

    public Boot hconfigFile(String file) {
        this.hconfigFile = Objects.requireNonNull(file, "file");
        return this;
    }

    public Boot property(String key, Object value) {
        properties.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
        return this;
    }

    public BootApplication start() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null)
            loader = Boot.class.getClassLoader();
        BootConfiguration configuration = new BootConfiguration(arguments, sources, hconfigFile, loader, properties);
        BootLauncher launcher = (environment, modules) -> {
            Hasor hasor = configuration.newHasor(environment);
            hasor.addModules(modules.toArray(new net.hasor.core.Module[0]));
            configuration.configureSources(hasor);
            return new BootApplication(hasor.build());
        };
        List<BootExtension> extensions = new ArrayList<>();
        ServiceLoader.load(BootExtension.class, loader).forEach(extensions::add);
        extensions.sort(Comparator.comparingInt(BootExtension::order).thenComparing(e -> e.getClass().getName()));
        for (int i = extensions.size() - 1; i >= 0; i--) {
            launcher = Objects.requireNonNull(extensions.get(i).configure(configuration, launcher), "launcher");
        }
        BootApplication application = launcher.start(null, List.of());
        try {
            AutoCloseable hook = SystemUtils.registerShutdownHook(() -> {
                application.close();
                return null;
            });
            application.onClose(hook);
            return application;
        } catch (Exception | Error e) {
            try {
                application.close();
            } catch (Throwable cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }
}
