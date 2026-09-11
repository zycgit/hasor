/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.members;
import net.hasor.core.BindInfo;
import net.hasor.core.spi.BindInfoAware;
import net.hasor.test.core.basic.pojo.PojoBean;
import net.hasor.test.core.basic.pojo.PojoBeanRef;

public class InjectBindInfoOk extends PojoBeanRef implements BindInfoAware {
    @Override
    public void setBindInfo(BindInfo<?> bindInfo) {
        PojoBean pojoBean = new PojoBean();
        if (bindInfo != null) {
            pojoBean.setUuid("create by BindInfoAware ,bindID is " + bindInfo.getBindID());
        } else {
            pojoBean.setUuid("create by BindInfoAware ,bindID is null.");
        }
        this.setPojoBean(pojoBean);
    }
}
