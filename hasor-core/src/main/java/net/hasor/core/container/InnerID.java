/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.container;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;
import net.hasor.core.ID;

record InnerID(String value) implements ID, Serializable {

    InnerID(String value) {
        this.value = Objects.requireNonNull(value, "id");
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ID other)) {
            return false;
        }
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + ID.class.getName() + "(value=" + value + ")";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ID.class;
    }

    private static final long serialVersionUID = 0;
}
