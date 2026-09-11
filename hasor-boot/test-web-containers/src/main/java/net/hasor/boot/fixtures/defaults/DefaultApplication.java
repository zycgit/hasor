/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.fixtures.defaults;

public class DefaultApplication implements net.hasor.web.WebModule {
    @Override
    public void loadModule(net.hasor.web.WebApiBinder binder) {
        binder.setResponseCharacter("UTF-8");
    }
}
