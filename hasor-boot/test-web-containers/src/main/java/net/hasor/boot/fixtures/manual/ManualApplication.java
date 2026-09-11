/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.manual;

import net.hasor.boot.fixtures.defaults.HelloController;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

public class ManualApplication implements WebModule {
    @Override
    public void loadModule(WebApiBinder binder) {
        binder.loadMappingTo(HelloController.class);
        binder.setEncodingCharacter("UTF-8", "UTF-8");
    }
}
