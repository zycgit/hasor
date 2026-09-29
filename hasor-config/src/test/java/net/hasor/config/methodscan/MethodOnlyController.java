/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.methodscan;
import net.hasor.core.Inject;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

public class MethodOnlyController {
    @Inject
    private StringBuilder greeting;

    @Get
    @MappingTo("/method/{id}")
    @MappingTo("/alias/{id}")
    public String read() {
        return this.greeting.toString();
    }

    @Get
    @MappingTo("/shared")
    public String shared() {
        return "get";
    }
}