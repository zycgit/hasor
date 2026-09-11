/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.basic;
import net.hasor.web.Controller;
import net.hasor.web.Invoker;

public class BasicController implements Controller {
    private boolean execute;
    private boolean doInit;

    public boolean isExecute() {
        return execute;
    }

    public void execute() {
        this.execute = true;
    }

    @Override
    public void initController(Invoker renderData) {
        this.doInit = true;
    }
}
