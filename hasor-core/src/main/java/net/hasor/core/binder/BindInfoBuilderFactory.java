/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.container.BindInfoContainer;
import net.hasor.core.container.ScopeContainer;
import net.hasor.core.container.SpiCallerContainer;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年7月2日
 */
public interface BindInfoBuilderFactory {
    Settings getSettings();

    SpiCallerContainer getSpiContainer();

    BindInfoContainer getBindInfoContainer();

    ScopeContainer getScopeContainer();
}
