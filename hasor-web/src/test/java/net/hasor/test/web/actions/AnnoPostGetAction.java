/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.Post;

public class AnnoPostGetAction {
    private boolean doPost;
    private boolean doGet;

    public boolean isDoPost() {
        return doPost;
    }

    public boolean isDoGet() {
        return doGet;
    }

    @Post
    public void doPost() {
        doPost = true;
    }

    @Get
    public void doGet() {
        doGet = true;
    }
}
