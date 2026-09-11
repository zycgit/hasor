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

public class HashRemainderScope implements Scope {
    private int                          modulus   = 0;
    private       int                          remainder = 0;
    private final HashMap<Object, Supplier<?>> scopeMap  = new HashMap<>();

    public HashRemainderScope(int modulus, int remainder) {
        this.modulus = modulus;
        this.remainder = remainder;
    }

    @Override
    public String toString() {
        return "HashRemainderScope{" + "modulus=" + modulus + ", remainder=" + remainder + '}';
    }

    public <T> Supplier<T> scope(Object key, final Supplier<T> provider) {
        Supplier<?> returnData = this.scopeMap.get(key);
        if (returnData == null) {
            T t = provider.get();
            if (t == null) {
                return provider;
            }
            //
            if (t.hashCode() % modulus == remainder) {
                Supplier<T> newSingleProvider = Provider.of(provider).asSingle();
                returnData = this.scopeMap.putIfAbsent(key, newSingleProvider);
                if (returnData == null) {
                    returnData = newSingleProvider;
                }
            } else {
                returnData = provider;
            }
            //
        }
        return (Supplier<T>) returnData;
    }

    public int getModulus() {
        return modulus;
    }

    public int getRemainder() {
        return remainder;
    }

    public HashMap<Object, Supplier<?>> getScopeMap() {
        return scopeMap;
    }
}
