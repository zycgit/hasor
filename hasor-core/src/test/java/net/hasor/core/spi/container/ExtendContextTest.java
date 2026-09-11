/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi.container;
import net.hasor.core.Hasor;
import net.hasor.core.TypeSupplier;
import net.hasor.test.core.basic.pojo.PojoBean;
import org.junit.Test;

public class ExtendContextTest {
    @Test
    public void test1() {
        PojoBean pojoBean = new PojoBean();
        TypeSupplier objectTypeSupplier = Hasor.create().build(apiBinder -> {
            apiBinder.bindType(PojoBean.class).toInstance(pojoBean);
        }).wrapTypeSupplier();
        assert objectTypeSupplier.get(PojoBean.class) == pojoBean;
    }
}
