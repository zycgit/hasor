/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.bean;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.test.core.basic.factory.FaceFactory;
import net.hasor.test.core.basic.pojo.SampleBean;
import net.hasor.test.core.basic.pojo.SampleFace;
import org.junit.Test;

public class BeanTest {
    @Test
    public void beanProvider() {
        FaceFactory factory = new FaceFactory();
        //
        AppContext appContext = Hasor.create().build(apiBinder -> {
            apiBinder.bindType(SampleBean.class).toTypeSupplier(factory);
            apiBinder.bindType(SampleFace.class).toTypeSupplier(factory);
        });
        //
        SampleBean sampleBean1 = appContext.getInstance(SampleBean.class);
        SampleFace sampleBean2 = appContext.getInstance(SampleFace.class);
        //
        assert sampleBean1 != sampleBean2;
        assert factory.getTarget1() == sampleBean1;
        assert factory.getTarget2() == sampleBean2;
    }
}
