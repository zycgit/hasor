/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.throwerr;
import javax.servlet.ServletException;
import net.hasor.web.annotation.Any;

public class ThrowServletErrorAction {
    @Any
    public void doPost() throws Exception {
        throw new ServletException("ServletException - doPost");
    }
}
