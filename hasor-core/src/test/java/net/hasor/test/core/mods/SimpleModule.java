/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.mods;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import net.hasor.test.core.basic.init.WithoutAnnoCallInitBean;

/**
 * 模块
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年11月9日
 */
public class SimpleModule extends WithoutAnnoCallInitBean implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        this.init();
    }
}
