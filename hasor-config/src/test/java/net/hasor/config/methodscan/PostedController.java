/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.methodscan;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.Post;

public class PostedController {
    @Post
    @MappingTo("/shared")
    public String create() {
        return "post";
    }
}