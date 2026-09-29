/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.methodmapping;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.render.RenderType;

@MappingTo({ "/v1/", "/v2" })
@RenderType("text")
public class PrefixedAction {
    @Get
    @MappingTo({ "/", "/health" })
    public String health() {
        return "healthy";
    }
}