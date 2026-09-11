/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.scope;
public class CustomHashBean {
    private final int hashCode;

    public CustomHashBean(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
