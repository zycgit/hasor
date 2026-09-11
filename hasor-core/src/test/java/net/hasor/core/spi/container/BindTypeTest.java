/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi.container;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import org.junit.Test;

public class BindTypeTest {
    @Test
    public void test1() {
        AppContext appContext = Hasor.create().build(apiBinder -> {
            apiBinder.bindType(BindTypeTest.class).uniqueName().toInstance(new BindTypeTest());
            apiBinder.bindType(BindTypeTest.class).uniqueName().toInstance(new BindTypeTest());
        });
        assert appContext.findBindingBean(BindTypeTest.class).size() == 2;
    }
}
