/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.defaults;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.Post;
import net.hasor.web.annotation.QueryParameter;
import net.hasor.web.render.RenderType;

@RenderType("text")
public class MethodController {
    @Get
    @MappingTo("/method")
    @MappingTo("/method-alias")
    public String read(@QueryParameter("name") String name) {
        return "read:" + name;
    }

    @Post
    @MappingTo("/method")
    public String create(@QueryParameter("name") String name) {
        return "create:" + name;
    }

    @MappingTo("/method/any")
    public String any() {
        return "any";
    }
}