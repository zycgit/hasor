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
import net.hasor.test.core.docs.CustomBean;
import net.hasor.test.core.docs.OrderManager;
import org.junit.Test;

public class DocsTest {
    @Test
    public void doc_1() {
        CustomBean customBean = Hasor.create().build().getInstance(CustomBean.class);
        assert customBean != null;
        assert customBean.callFoo() != null;
    }

    @Test
    public void doc_2() {
        OrderManager customBean = Hasor.create().build().getInstance(OrderManager.class);
        assert customBean.getStockBean() == null;
        assert customBean.getStockBeanTest() == null;
    }
}
