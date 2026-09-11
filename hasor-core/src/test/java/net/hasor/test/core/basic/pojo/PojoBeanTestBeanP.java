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
public class PojoBeanTestBeanP {
    private Date   abc1;
    private int    abc2;
    private Object abc3;
    private Method abc4;

    public Date getAbc1() {
        return abc1;
    }

    public void setAbc1(Date abc1) {
        this.abc1 = abc1;
    }

    public int getAbc2() {
        return abc2;
    }

    public void setAbc2(int abc2) {
        this.abc2 = abc2;
    }

    public Object getAbc3() {
        return abc3;
    }

    public void setAbc3(Object abc3) {
        this.abc3 = abc3;
    }

    public Method getAbc4() {
        return abc4;
    }

    public void setAbc4(Method abc4) {
        this.abc4 = abc4;
    }

    public void doInit() {
    }

    public void doDestroy() {
    }
}
