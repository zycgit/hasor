/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class InvocationHandlerTest extends AbstractBinderDataTest {
    @Test
    public void handlerTest() {
        Map<Class<?>, Object> supportMap = new HashMap<>();
        ApiBinderInvocationHandler handler1 = new ApiBinderInvocationHandler(supportMap);
        assert handler1.supportMap().isEmpty();
        //
        Object val = new Object();
        supportMap.put(Object.class, val);
        ApiBinderInvocationHandler handler2 = new ApiBinderInvocationHandler(supportMap);
        assert handler2.supportMap().size() == 1;
        assert handler2.supportMap().get(Object.class) == val;
        //
        try {
            supportMap.put(ApiBinderInvocationHandler.class, null);
            new ApiBinderInvocationHandler(supportMap);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("this method is not support -> ");
        }
    }
}
