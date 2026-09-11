/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.async;
import net.hasor.web.annotation.Async;
import net.hasor.web.annotation.Get;

@Async
public class GetAsyncAction extends AbstractAsyncAction {
    private boolean doCall;

    public boolean isDoCall() {
        return doCall;
    }

    @Get
    public void doCall() {
        super.initLocalObject();
        doCall = true;
    }
}
