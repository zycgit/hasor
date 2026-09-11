/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.conflict;

import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo("/duplicate/{first}")
public class FirstController {
    @Get
    public void execute() {
    }
}
