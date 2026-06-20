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