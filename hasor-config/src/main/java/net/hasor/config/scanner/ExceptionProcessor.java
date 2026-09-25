/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;
import net.hasor.config.Bean;
import net.hasor.config.web.Exception;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.web.ExceptionHandler;
import net.hasor.web.WebApiBinder;

/** Registers managed handler factories without creating their beans during module loading. */
public final class ExceptionProcessor implements AnnotationProcessor<Method> {
    private final BeanProcessor beanProcessor = new BeanProcessor();

    @Override
    public List<Class<? extends Annotation>> annotationTypes() {
        return List.of(Exception.class);
    }

    @Override
    public void process(ApiBinder binder, List<Method> methods) {
        for (Method method : methods) {
            Exception annotation = method.getAnnotation(Exception.class);
            if (annotation == null) {
                continue;
            }
            WebApiBinder webBinder = binder.tryCast(WebApiBinder.class);
            if (webBinder == null) {
                throw new IllegalStateException("@Exception requires a Web application: " + method);
            }
            if (!ExceptionHandler.class.isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("@Exception factory must return an ExceptionHandler: " + method);
            }
            if (annotation.value().length == 0) {
                throw new IllegalArgumentException("@Exception must declare at least one exception type: " + method);
            }

            Bean bean = method.getAnnotation(Bean.class);
            BindInfo<?> binding;
            if (bean == null) {
                binding = this.beanProcessor.bindBeanMethod(binder, method, null);
            } else {
                String beanId = bean.value().isBlank() ? method.getName() : bean.value();
                binding = binder.findBindingRegister(method.getReturnType()).stream().filter(info -> beanId.equals(info.getBindID())).findFirst().orElseThrow();
            }
            Supplier<?> factory = binder.getProvider(binding);
            for (Class<? extends Throwable> exceptionType : annotation.value()) {
                this.register(webBinder, exceptionType, factory);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void register(WebApiBinder binder, Class<E> exceptionType, Supplier<?> factory) {
        binder.addExceptionHandler(exceptionType, (invoker, exception) -> {
            ExceptionHandler<E> handler = (ExceptionHandler<E>) factory.get();
            return handler.handleException(invoker, exception);
        });
    }
}
