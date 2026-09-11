/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.async;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.Async;

@Async
public class ClassAsyncAction extends AbstractAsyncAction {
    private boolean execute;

    public boolean isExecute() {
        return execute;
    }

    @Any
    public void execute() {
        super.initLocalObject();
        this.execute = true;
    }
}
