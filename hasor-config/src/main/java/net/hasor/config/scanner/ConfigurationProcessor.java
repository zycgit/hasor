/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.provider.Provider;
import net.hasor.config.Configuration;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.core.Module;

/** 处理 @Configuration 配置类及其模块装配。 */
public final class ConfigurationProcessor implements AnnotationProcessor<Class<?>> {
    private static final String                      CONFIGURATION_PROCESSED = "hasor.config.processed";
    private static final BeanProcessor               BEAN_PROCESSOR          = new BeanProcessor();
    private static final AnnotationProcessor<Method> EXCEPTION_PROCESSOR     = createExceptionProcessor();

    private static AnnotationProcessor<Method> createExceptionProcessor() {
        try {
            return new ExceptionProcessor();
        } catch (NoClassDefFoundError ignored) {
            return null;
        }
    }

    @Override
    public List<Class<? extends Annotation>> annotationTypes() {
        return List.of(Configuration.class);
    }

    /** 加载已发现或明确指定的配置类，不触发包扫描。 */
    @Override
    public void process(ApiBinder binder, List<Class<?>> types) {
        for (Class<?> type : types) {
            loadConfiguration(binder, type);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void loadConfiguration(ApiBinder apiBinder, Class<?> configType) {
        this.checkConfigurationType(configType);
        BindInfo<?> existing = apiBinder.findBindingRegister("", configType);
        if (existing != null && Boolean.TRUE.equals(existing.getMetaData(CONFIGURATION_PROCESSED))) {
            return;
        }

        BindInfo<?> configurationInfo;
        Object moduleInstance = null;
        boolean moduleType = Module.class.isAssignableFrom(configType);
        if (moduleType) {
            try {
                moduleInstance = ClassUtils.newInstance(configType);
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }

            Object configurationInstance = moduleInstance;
            Provider<Object> provider = ((Provider<Object>) () -> configurationInstance).asSingle();
            configurationInfo = apiBinder.bindType((Class) configType).toProvider(provider).asEagerSingleton().toInfo();
            configurationInfo.setMetaData(CONFIGURATION_PROCESSED, true);
            configurationInfo.setMetaData(Module.MODULE_INSTALLED, true);
            Object instance = moduleInstance;
            apiBinder.lazyLoad(appContext -> appContext.justInject(instance, configType));
        } else {
            configurationInfo = apiBinder.bindType(configType).asEagerSingleton().toInfo();
            configurationInfo.setMetaData(CONFIGURATION_PROCESSED, true);
        }

        List<Method> methods = Arrays.asList(configType.getDeclaredMethods());
        BEAN_PROCESSOR.process(apiBinder, methods);
        if (EXCEPTION_PROCESSOR != null) {
            try {
                EXCEPTION_PROCESSOR.process(apiBinder, methods);
            } catch (Throwable e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        if (moduleType) {
            try {
                apiBinder.installModule((Module) moduleInstance);
            } catch (Throwable e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }
    }

    private void checkConfigurationType(Class<?> configType) {
        Objects.requireNonNull(configType, "configurationType is null.");
        if (configType.getAnnotation(Configuration.class) == null) {
            throw new IllegalArgumentException(configType.getName() + " must be annotated with @Configuration.");
        }

        int modifiers = configType.getModifiers();
        if (configType.isInterface() || configType.isEnum() || configType.isArray() || Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException(configType.getName() + " must be a concrete class.");
        }
    }
}
