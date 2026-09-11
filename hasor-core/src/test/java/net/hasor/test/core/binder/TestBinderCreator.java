/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.binder;
import net.hasor.core.ApiBinder;
import net.hasor.core.binder.ApiBinderCreator;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class TestBinderCreator implements ApiBinderCreator<TestBinder> {
    @Override
    public TestBinder createBinder(ApiBinder apiBinder) {
        return new TestBinderImpl(apiBinder);
    }
}
