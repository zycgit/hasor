/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core;
import net.hasor.core.BindInfo;

public class MockBindInfo implements BindInfo<Object> {
    @Override
    public String getBindID() {
        return null;
    }

    @Override
    public String getBindName() {
        return null;
    }

    @Override
    public Class<Object> getBindType() {
        return null;
    }

    @Override
    public Object getMetaData(String key) {
        return null;
    }

    @Override
    public void setMetaData(String key, Object value) {
    }

    @Override
    public void removeMetaData(String key) {
    }
}
