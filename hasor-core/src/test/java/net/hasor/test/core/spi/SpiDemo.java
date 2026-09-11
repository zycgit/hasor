/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.spi;
import net.hasor.core.BindInfo;
import net.hasor.core.Spi;
import net.hasor.core.spi.CreatorProvisionListener;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年9月7日
 */
@Spi(CreatorProvisionListener.class)
public class SpiDemo implements CreatorProvisionListener {
    private Object      newObject;
    private BindInfo<?> bindInfo;

    public Object getNewObject() {
        return newObject;
    }

    public BindInfo<?> getBindInfo() {
        return bindInfo;
    }

    @Override
    public void beanCreated(Object newObject, BindInfo<?> bindInfo) throws Throwable {
        this.newObject = newObject;
        this.bindInfo = bindInfo;
    }
}
