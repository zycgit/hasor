/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;

/**
 * WebModule
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-11-4
 */
@FunctionalInterface
public interface WebModule extends Module {
    @Override
    default void loadModule(final ApiBinder apiBinder) throws Throwable {
        WebApiBinder webApiBinder = apiBinder.tryCast(WebApiBinder.class);
        if (webApiBinder == null) {
            throw new Module.IgnoreModuleException();
        }
        this.loadModule(webApiBinder);
    }

    void loadModule(WebApiBinder apiBinder) throws Throwable;
}
