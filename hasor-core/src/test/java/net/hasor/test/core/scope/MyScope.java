/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.scope;
import java.util.HashMap;
import java.util.function.Supplier;
import net.hasor.cobble.provider.Provider;
import net.hasor.cobble.provider.Scope;

public class MyScope implements Scope {
    private final HashMap<Object, Supplier<?>> scopeMap = new HashMap<>();

    public <T> Supplier<T> scope(Object key, final Supplier<T> provider) {
        Supplier<?> returnData = this.scopeMap.get(key);
        if (returnData == null) {
            Supplier<T> newSingleProvider = Provider.of(provider).asSingle();
            returnData = this.scopeMap.putIfAbsent(key, newSingleProvider);
            if (returnData == null) {
                returnData = newSingleProvider;
            }
        }
        return (Supplier<T>) returnData;
    }
}
