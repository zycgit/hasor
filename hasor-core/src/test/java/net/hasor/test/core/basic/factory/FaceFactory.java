/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.factory;
import net.hasor.core.TypeSupplier;
import net.hasor.test.core.basic.pojo.SampleBean;
import net.hasor.test.core.basic.pojo.SampleFace;

/**
 * TypeSupplier
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-09-22
 */
public class FaceFactory implements TypeSupplier {
    private final SampleBean target1 = new SampleBean();
    private final SampleFace target2 = new SampleBean();

    public SampleBean getTarget1() {
        return target1;
    }

    public SampleFace getTarget2() {
        return target2;
    }

    public <T> T get(Class<? extends T> targetType) {
        if (targetType == SampleBean.class) {
            return (T) target1;
        }
        if (targetType == SampleFace.class) {
            return (T) target2;
        }
        return null;
    }
}
