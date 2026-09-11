/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.demo.boot.web;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.boot.web.WebServers;

/**
 * Minimal executable Hasor Boot Web demo.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public class DemoHasorBootWebApplication implements WebModule {
    private static final Logger logger = LoggerFactory.getLogger(DemoHasorBootWebApplication.class);

    public static void main(String[] args) throws Exception {
        WebServers.run(args, DemoHasorBootWebApplication.class).join();
    }

    @Override
    public void loadModule(WebApiBinder apiBinder) {
        apiBinder.setEncodingCharacter("utf-8", "utf-8");
        apiBinder.loadMappingTo(HelloWebAction.class);
    }

    @Override
    public void onStart(AppContext appContext) {
        logger.info("hello Hasor Boot Web");
    }

    @Override
    public void onStop(AppContext appContext) {
        logger.info("bye Hasor Boot Web");
    }
}
