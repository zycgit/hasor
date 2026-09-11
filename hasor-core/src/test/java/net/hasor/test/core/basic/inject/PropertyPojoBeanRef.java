/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject;
import net.hasor.core.Inject;
import net.hasor.test.core.basic.pojo.PojoBean;

public class PropertyPojoBeanRef {
    @Inject
    private PojoBean pojoBean;

    public PojoBean getPojoBean() {
        return pojoBean;
    }

    public void setPojoBean(PojoBean pojoBean) {
        this.pojoBean = pojoBean;
    }
}
