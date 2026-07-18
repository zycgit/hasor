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
package net.hasor.config.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.provider.Provider;
import net.hasor.config.Bean;
import net.hasor.config.Configuration;
import net.hasor.core.*;
import net.hasor.core.Module;

/** Loads {@link Configuration @Configuration} classes and their {@link Bean @Bean} methods. */
public class ConfigurationModule implements Module {
    public static final String        CONFIGURATION_PROCESSED = "hasor.config.processed";
    private final       Set<Class<?>> configurationTypes;

    public ConfigurationModule(Class<?>... configurationTypes) {
        Objects.requireNonNull(configurationTypes, "configurationTypes is null.");
        this.configurationTypes = new LinkedHashSet<>(Arrays.asList(configurationTypes));
    }

    public static ConfigurationModule of(Class<?>... configurationTypes) {
        return new ConfigurationModule(configurationTypes);
    }

    /** Scans bounded packages for configuration classes. */
    public static Module scan(String... scanPackages) {
        return apiBinder -> {
            Set<Class<?>> configTypes = apiBinder.findClass(Configuration.class, scanPackages);
            new ConfigurationModule(configTypes.toArray(new Class<?>[0])).loadModule(apiBinder);
        };
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        for (Class<?> configType : this.configurationTypes) {
            this.loadConfiguration(apiBinder, configType);
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
                throw net.hasor.cobble.ExceptionUtils.toRuntime(e);
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
        Supplier<?> configuration = apiBinder.getProvider(configType);

        for (Method method : configType.getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                this.bindBeanMethod(apiBinder, configuration, method, bean);
            }
        }

        if (moduleType) {
            try {
                apiBinder.installModule((Module) moduleInstance);
            } catch (Throwable e) {
                throw net.hasor.cobble.ExceptionUtils.toRuntime(e);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void bindBeanMethod(ApiBinder apiBinder, Supplier<?> configuration, Method method, Bean bean) {
        this.checkBeanMethod(method);
        method.trySetAccessible();
        Supplier<?>[] parameters = Arrays.stream(method.getParameterTypes()).map(apiBinder::getProvider).toArray(Supplier[]::new);
        CopyOnWriteArrayList<Object> createdBeans = new CopyOnWriteArrayList<>();
        Provider<Object> factory = () -> {
            Object instance = this.invoke(method, configuration.get(), parameters);
            this.invokeLifecycle(instance, bean.initMethod());
            if (!bean.destroyMethod().isBlank()) {
                createdBeans.add(instance);
            }
            return instance;
        };
        Supplier<?> scopedFactory = bean.singleton() ? factory.asSingle() : factory;
        if (!bean.destroyMethod().isBlank()) {
            apiBinder.onShutdown((Consumer<AppContext>) appContext -> {
                for (Object instance : createdBeans) {
                    this.invokeLifecycle(instance, bean.destroyMethod());
                }
            });
        }

        ApiBinder.NamedBindingBuilder namedBuilder = apiBinder.bindType(method.getReturnType());
        ApiBinder.LinkedBindingBuilder builder = bean.value().isBlank() ? namedBuilder.idWith(method.getName()) : namedBuilder.bothWith(bean.value());
        ApiBinder.LifeBindingBuilder binding = builder.toProvider(scopedFactory);
        binding.metaData(CircularDependencyException.DEPENDENCY_DESCRIPTION, factoryMethodDescription(method));
        if (bean.singleton()) {
            binding.asEagerSingleton();
        } else {
            binding.asEagerPrototype();
        }
    }

    private String factoryMethodDescription(Method method) {
        String parameters = Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(java.util.stream.Collectors.joining(", "));
        return "@Bean " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + parameters + ")";
    }

    private void invokeLifecycle(Object instance, String methodName) {
        if (methodName.isBlank()) {
            return;
        }
        try {
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Lifecycle method not found: " + instance.getClass().getName() + "." + methodName + "()", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke lifecycle method: " + methodName, e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Lifecycle method failed: " + methodName, target);
        }
    }

    private Object invoke(Method method, Object configuration, Supplier<?>[] parameters) {
        Object[] args = Arrays.stream(parameters).map(Supplier::get).toArray();
        try {
            Object result = method.invoke(configuration, args);
            return Objects.requireNonNull(result, "@Bean method returned null: " + method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke @Bean method: " + method, e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (target instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("@Bean method failed: " + method, target);
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

    private void checkBeanMethod(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("@Bean method must not be static: " + method);
        }
        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("@Bean method must not be abstract: " + method);
        }
        if (method.getReturnType() == Void.TYPE) {
            throw new IllegalArgumentException("@Bean method must return a value: " + method);
        }
    }
}
