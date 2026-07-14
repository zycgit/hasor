/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.demo.boot.web;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.http.WebServers;

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
