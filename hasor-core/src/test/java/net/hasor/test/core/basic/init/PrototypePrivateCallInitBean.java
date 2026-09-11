/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.init;
import net.hasor.core.Init;
import net.hasor.core.Prototype;

/**
 * 一个Bean
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-1-3
 */
@Prototype
public class PrototypePrivateCallInitBean {
    private boolean init = false;

    public boolean isInit() {
        return init;
    }

    @Init
    private void init() {
        this.init = true;
    }
}
