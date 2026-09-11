/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.members;
import net.hasor.core.AppContext;
import net.hasor.core.spi.AppContextAware;
import net.hasor.test.core.basic.pojo.PojoBeanRef;

public class InjectAppContextFailed extends PojoBeanRef implements AppContextAware {
    @Override
    public void setAppContext(AppContext appContext) {
        throw new RuntimeException("test Inject Error");
    }
}
