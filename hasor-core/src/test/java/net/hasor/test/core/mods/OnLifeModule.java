/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.mods;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Module;

/**
 * 在模块中处理容器启动
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2013-9-14
 */
public class OnLifeModule implements Module {
    protected           Logger logger = LoggerFactory.getLogger(getClass());
    public static final String STR    = "say form Mod_1.";

    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.installModule(new SimpleModule());
        apiBinder.bindType(String.class).uniqueName().toInstance(STR);
    }

    @Override
    public void onStart(AppContext appContext) throws Throwable {
        logger.info("启动啦...");
    }

    @Override
    public void onStop(AppContext appContext) throws Throwable {
        logger.info("停止啦...");
    }
}
