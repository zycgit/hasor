/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.methodmapping;
import net.hasor.web.annotation.*;
import net.hasor.web.render.RenderType;

@RenderType("text")
public class MethodMappedAction {
    @Get
    @MappingTo("/items/{id}")
    public String read(@PathParameter("id") String id) {
        return "read:" + id;
    }

    @Post
    @MappingTo("/items/{id}")
    public String create(@PathParameter("id") String id) {
        return "create:" + id;
    }

    @MappingTo("/echo")
    public String echo(@QueryParameter("value") String value) {
        return value;
    }

    @MappingTo("/ping")
    @MappingTo("/alias")
    public String ping() {
        return "pong";
    }

    @Get
    @MappingTo("/fallback")
    public String get() {
        return "get";
    }

    @MappingTo("/fallback")
    public String fallback() {
        return "fallback";
    }

    @Get
    public String unmapped() {
        return "unmapped";
    }
}