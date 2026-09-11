/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.demo.boot.web;
import java.io.IOException;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

/**
 * Simple MVC action used by the embedded Web demo.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
@MappingTo("/hello")
public class HelloWebAction {
    @Get
    public void execute(Invoker invoker) throws IOException {
        invoker.getHttpResponse().setContentType("text/plain;charset=UTF-8");
        invoker.getHttpResponse().getWriter().write("hello Hasor Boot Web");
    }
}
