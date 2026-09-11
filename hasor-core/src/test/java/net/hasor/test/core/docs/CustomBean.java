/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.docs;
import net.hasor.core.ConstructorBy;
import net.hasor.core.Inject;

public class CustomBean {
    private FunBean funBean = null;

    @ConstructorBy
    public CustomBean(@Inject() FunBean funBean) {
        this.funBean = funBean;
    }

    public FunBean callFoo() {
        return this.funBean.foo();
    }
}
