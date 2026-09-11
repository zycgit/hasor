/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.container;

import net.hasor.core.AppContext;
import net.hasor.core.CircularDependencyException;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import org.junit.Test;
import static org.junit.Assert.*;

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
