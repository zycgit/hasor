/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.core.binder;
import net.hasor.core.HasorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HasorUtils.class })
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
