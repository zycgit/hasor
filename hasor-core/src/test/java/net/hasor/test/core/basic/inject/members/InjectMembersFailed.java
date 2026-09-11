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
import net.hasor.core.spi.InjectMembers;
import net.hasor.test.core.basic.pojo.PojoBeanRef;

public class InjectMembersFailed extends PojoBeanRef implements InjectMembers {
    @Override
    public void doInject(AppContext appContext) {
        throw new RuntimeException("test Inject Error");
    }
}
