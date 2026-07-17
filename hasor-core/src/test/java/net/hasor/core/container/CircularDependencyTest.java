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
package net.hasor.core.container;

import net.hasor.core.AppContext;
import net.hasor.core.CircularDependencyException;
import net.hasor.core.Hasor;
import org.junit.Test;
import net.hasor.core.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CircularDependencyTest {
    @Test
    public void fieldCycleShouldReportDependencyPath() {
        AppContext context = Hasor.create().build(apiBinder -> {
            apiBinder.bindType(ServiceA.class);
            apiBinder.bindType(ServiceB.class);
        });

        try {
            context.getInstance(ServiceA.class);
            fail("CircularDependencyException expected.");
        } catch (CircularDependencyException e) {
            assertEquals(3, e.getDependencyPath().size());
            assertTrue(e.getMessage().contains("ServiceA"));
            assertTrue(e.getMessage().contains("ServiceB"));
            assertTrue(e.getMessage().contains("depends on"));
            assertTrue(e.getMessage().contains("cycle closes here"));
        }
    }

    public static class ServiceA {
        @Inject
        private ServiceB serviceB;
    }

    public static class ServiceB {
        @Inject
        private ServiceA serviceA;
    }
}
