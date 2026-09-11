/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.context;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;

/**
 * 模块
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年11月9日
 */
public class ConfigModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(String.class).toInstance("config");
    }
}
