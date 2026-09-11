/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.pojo;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class PojoBeanTestBeanC {
    private final Date abc1;
    private final int  abc2;
    private final Object abc3;
    private final Method abc4;

    public PojoBeanTestBeanC(Date abc1, int abc2, Object abc3, Method abc4) {
        this.abc1 = abc1;
        this.abc2 = abc2;
        this.abc3 = abc3;
        this.abc4 = abc4;
    }

    public void doInit() {
    }

    public void doDestroy() {
    }
}
