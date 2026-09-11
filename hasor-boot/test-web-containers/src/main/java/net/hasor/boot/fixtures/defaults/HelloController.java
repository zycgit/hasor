/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.defaults;

import javax.servlet.http.HttpServletResponse;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo({ "/hello", "/override" })
public class HelloController {
    @Get
    public void hello(HttpServletResponse response) throws Exception {
        response.getWriter().write("你好");
    }
}
