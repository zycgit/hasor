/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.hasor.cobble.dynamic.DynamicProperty;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.provider.Scope;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.core.EventContext;
import net.hasor.core.TypeSupplier;
import net.hasor.core.spi.SpiJudge;

/**
 * 标准的 {@link ApiBinder} 接口包装类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-12
 */
public class ApiBinderWrap implements ApiBinder {
    protected static Logger    logger = LoggerFactory.getLogger(ApiBinderWrap.class);
    private final    ApiBinder apiBinder;

    public ApiBinderWrap(ApiBinder apiBinder) {
        this.apiBinder = Objects.requireNonNull(apiBinder);
    }

    @Override
    public Settings getSettings() {
        return this.apiBinder.getSettings();
    }

    @Override
    public EventContext getEventContext() {
        return this.apiBinder.getEventContext();
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return this.apiBinder.getResourceLoader();
    }

    @Override
    public Object getContext() {
        return this.apiBinder.getContext();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.apiBinder.getClassLoader();
    }

    @Override
    public Set<Class<?>> findClass(Class<?> featureType, String... scanPackages) {
        return this.apiBinder.findClass(featureType, scanPackages);
    }

    @Override
    public <T extends ApiBinder> T tryCast(Class<T> castApiBinder) {
        return this.apiBinder.tryCast(castApiBinder);
    }

    @Override
    public ApiBinder installModule(final net.hasor.core.Module... module) throws Throwable {
        return this.apiBinder.installModule(module);
    }

    @Override
    public boolean isSingleton(BindInfo<?> bindInfo) {
        return this.apiBinder.isSingleton(bindInfo);
    }

    @Override
    public boolean isSingleton(Class<?> targetType) {
        return this.apiBinder.isSingleton(targetType);
    }

    @Override
    public ApiBinder loadModule(Class<?> moduleType, TypeSupplier typeSupplier) {
        return this.apiBinder.loadModule(moduleType, typeSupplier);
    }

    @Override
    public void bindInterceptor(String matcherExpression, MethodInterceptor interceptor) {
        this.apiBinder.bindInterceptor(matcherExpression, interceptor);
    }

    @Override
    public void bindInterceptor(Predicate<Class<?>> matcherClass, Predicate<Method> matcherMethod, MethodInterceptor interceptor) {
        this.apiBinder.bindInterceptor(matcherClass, matcherMethod, interceptor);
    }

    @Override
    public LinkedBindingBuilder<DynamicProperty> dynamicProperty(Predicate<Class<?>> matcherClass, String name, Class<?> propertyType) {
        return this.apiBinder.dynamicProperty(matcherClass, name, propertyType);
    }

    @Override
    public LinkedBindingBuilder<DynamicProperty> dynamicReadOnlyProperty(Predicate<Class<?>> matcherClass, String name, Class<?> propertyType) {
        return this.apiBinder.dynamicReadOnlyProperty(matcherClass, name, propertyType);
    }

    @Override
    public <T> BindInfo<T> getBindInfo(String bindID) {
        return this.apiBinder.getBindInfo(bindID);
    }

    @Override
    public <T> BindInfo<T> getBindInfo(Class<T> bindType) {
        return this.apiBinder.getBindInfo(bindType);
    }

    @Override
    public <T> List<BindInfo<T>> findBindingRegister(Class<T> bindType) {
        return this.apiBinder.findBindingRegister(bindType);
    }

    @Override
    public <T> BindInfo<T> findBindingRegister(String withName, Class<T> bindType) {
        return this.apiBinder.findBindingRegister(withName, bindType);
    }

    @Override
    public <T> NamedBindingBuilder<T> bindType(Class<T> type) {
        return this.apiBinder.bindType(type);
    }

    @Override
    public <T extends EventListener> void bindSpiListener(Class<T> spiType, Supplier<? extends T> listener) {
        this.apiBinder.bindSpiListener(spiType, listener);
    }

    @Override
    public <T extends EventListener> void bindSpiJudge(Class<T> spiType, Supplier<SpiJudge> spiJudgeSupplier) {
        this.apiBinder.bindSpiJudge(spiType, spiJudgeSupplier);
    }

    @Override
    public <T extends Scope> Supplier<T> bindScope(String scopeName, Supplier<T> scopeProvider) {
        return this.apiBinder.bindScope(scopeName, scopeProvider);
    }

    @Override
    public Supplier<Scope> findScope(String scopeName) {
        return this.apiBinder.findScope(scopeName);
    }
}
