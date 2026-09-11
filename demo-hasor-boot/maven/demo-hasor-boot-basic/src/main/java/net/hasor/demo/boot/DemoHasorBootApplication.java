/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.demo.boot;

import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.*;
import net.hasor.core.Module;
import net.hasor.core.info.Arguments;
/**
 * Minimal executable Hasor Boot demo.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
public class DemoHasorBootApplication implements Module {
    private static final Logger logger = LoggerFactory.getLogger(DemoHasorBootApplication.class);
    @Inject
    private Arguments           arguments;
    @Inject
    private HelloService        helloService;

    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class).toInstance(new HelloService("Hasor Boot"));
    }

    @Override
    public void onStart(AppContext appContext) {
        logger.info(this.helloService.sayHello(this.arguments));
    }

    @Override
    public void onStop(AppContext appContext) {
        logger.info(this.helloService.sayGoodbye(this.arguments));
    }
}
