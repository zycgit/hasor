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

public class DuplicateAction {
    @Get
    @MappingTo("/duplicate")
    public void first() {
    }

    @Get
    @MappingTo("/duplicate")
    public void second() {
    }
}