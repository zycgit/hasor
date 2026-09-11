/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.hasor.cobble.dynamic.DynamicProperty;
import net.hasor.cobble.dynamic.ReadWriteType;
import net.hasor.cobble.provider.Provider;

/**
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2020-09-29
 */
public class DelegateBindInfoAdapter implements Supplier<DynamicProperty>, DynamicProperty {
    private final Predicate<Class<?>>                 matcherClass;
    private final String                              propertyName;
    private final Class<?>                            propertyType;
    private final Supplier<? extends DynamicProperty> propertyDelegate;
    private final ReadWriteType                       rwType;

    public DelegateBindInfoAdapter(Predicate<Class<?>> matcherClass, String propertyName, Class<?> propertyType, Supplier<? extends DynamicProperty> propertyDelegate, ReadWriteType rwType) {
        this.matcherClass = matcherClass;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.propertyDelegate = Provider.of(propertyDelegate).asSingle();
        this.rwType = rwType;
    }

    public Predicate<Class<?>> getMatcherClass() {
        return this.matcherClass;
    }

    public String getName() {
        return this.propertyName;
    }

    public Class<?> getType() {
        return this.propertyType;
    }

    public ReadWriteType getRwType() {
        return this.rwType;
    }

    @Override
    public DynamicProperty get() {
        return this;
    }

    @Override
    public Object get(Object target) throws Throwable {
        return this.propertyDelegate.get().get(target);
    }

    @Override
    public void set(Object target, Object newValue) throws Throwable {
        this.propertyDelegate.get().set(target, newValue);
    }
}
