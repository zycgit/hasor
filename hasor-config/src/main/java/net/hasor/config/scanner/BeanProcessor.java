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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.hasor.cobble.provider.Provider;
import net.hasor.config.Bean;
import net.hasor.core.ApiBinder;
import net.hasor.core.ApiBinder.LifeBindingBuilder;
import net.hasor.core.ApiBinder.LinkedBindingBuilder;
import net.hasor.core.ApiBinder.NamedBindingBuilder;
import net.hasor.core.AppContext;
import net.hasor.core.CircularDependencyException;

/** 处理配置类中 @Bean 方法的注册与生命周期。 */
public final class BeanProcessor implements AnnotationProcessor<Method> {
    @Override
    public List<Class<? extends Annotation>> annotationTypes() {
        return List.of(Bean.class);
    }

    @Override
    public void process(ApiBinder binder, List<Method> methods) {
        for (Method method : methods) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                bindBeanMethod(binder, binder.getProvider(method.getDeclaringClass()), method, bean);
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

        NamedBindingBuilder namedBuilder = apiBinder.bindType(method.getReturnType());
        LinkedBindingBuilder builder = bean.value().isBlank() ? namedBuilder.idWith(method.getName()) : namedBuilder.bothWith(bean.value());
        LifeBindingBuilder binding = builder.toProvider(scopedFactory);
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
            if (target instanceof RuntimeException ee) {
                throw ee;
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
            if (target instanceof RuntimeException ee) {
                throw ee;
            }
            if (target instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("@Bean method failed: " + method, target);
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
