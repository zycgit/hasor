/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.constructor;
import net.hasor.test.core.basic.pojo.PojoBean;

public class NativeConstructorPojoBeanRef2 {
    private PojoBean pojoBean;
    private PojoBean pojoBean2;

    public NativeConstructorPojoBeanRef2(PojoBean pojoBean) {
        this.pojoBean = pojoBean;
    }

    public NativeConstructorPojoBeanRef2(PojoBean pojoBean1, PojoBean pojoBean2) {
        this.pojoBean = pojoBean1;
        this.pojoBean2 = pojoBean2;
    }

    public NativeConstructorPojoBeanRef2() {
        this.pojoBean = new PojoBean();
        this.pojoBean.setUuid("default");
    }

    public PojoBean getPojoBean() {
        return pojoBean;
    }

    public void setPojoBean(PojoBean pojoBean) {
        this.pojoBean = pojoBean;
    }

    public PojoBean getPojoBean2() {
        return pojoBean2;
    }

    public void setPojoBean2(PojoBean pojoBean2) {
        this.pojoBean2 = pojoBean2;
    }
}
