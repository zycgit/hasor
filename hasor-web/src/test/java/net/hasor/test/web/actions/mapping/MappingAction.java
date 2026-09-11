/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.mapping;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.HttpMethod;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.Post;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-08
 */
@MappingTo("/mappingto_b.do")
public class MappingAction {
    @Get
    public void execute1() {
    }

    @Post
    public void execute2() {
    }

    @HttpMethod({ "ADD", HttpMethod.DELETE })
    public void execute3() {
    }
}
