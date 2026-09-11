/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.demo.boot;

import net.hasor.core.info.Arguments;

/**
 * Service bound by the demo Hasor module.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
public class HelloService {
    private final String name;

    public HelloService(String name) {
        this.name = name;
    }

    public String sayHello(Arguments args) {
        return "hello " + this.name + " args=" + args;
    }

    public String sayGoodbye(Arguments args) {
        return "bye " + this.name + " args=" + args;
    }
}
