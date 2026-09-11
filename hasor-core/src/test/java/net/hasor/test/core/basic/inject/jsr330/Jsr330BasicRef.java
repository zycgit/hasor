/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.jsr330;
import javax.inject.Inject;
import net.hasor.test.core.basic.pojo.PojoBean;

public class Jsr330BasicRef {
    @ByOrder
    private PojoBean pojoBean1;
    //
    @Inject
    private PojoBean pojoBean2;
    //
    @Inject
    @ByOrder
    private PojoBean pojoBean3;
    //
    @ByOrder
    @Inject
    private PojoBean pojoBean4;
    //
    //
    //
    @ByOrder
    @Noise
    @Inject
    private PojoBean pojoBean5;
    @ByOrder
    @Inject
    @Noise
    private PojoBean pojoBean6;
}
