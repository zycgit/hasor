/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.constructor;
import net.hasor.core.ConstructorBy;
import net.hasor.test.core.basic.pojo.PojoBean;

public class MultipleConstructorPojoBeanRef {
    private       int      index = 0;
    private final PojoBean pojoBean;

    @ConstructorBy
    public MultipleConstructorPojoBeanRef(PojoBean pojoBean) {
        this.index = 1;
        this.pojoBean = pojoBean;
    }

    @ConstructorBy
    public MultipleConstructorPojoBeanRef(PojoBean pojoBean1, PojoBean pojoBean2) {
        this.index = 2;
        this.pojoBean = pojoBean2;
    }

    public PojoBean getPojoBean() {
        return pojoBean;
    }

    public int getIndex() {
        return index;
    }
}
