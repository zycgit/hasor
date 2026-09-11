/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.lang.reflect.Modifier;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.binder.ResourceDef;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResourceProcessorTest extends AbstractTest {
    @Test
    public void processorIsPackagePrivateAndNotAContainerBean() throws Throwable {
        assertFalse(Modifier.isPublic(ResourceProcessor.class.getModifiers()));
        assertFalse(Modifier.isPublic(ResourceHandler.class.getModifiers()));
        try (AppContext app = Hasor.create(servlet30("/")).build()) {
            assertTrue(app.findBindingBean(ResourceProcessor.class).isEmpty());
            assertTrue(app.findBindingBean(ResourceHandler.class).isEmpty());
            assertEquals(0, app.getInstance(ResourceDef[].class).length);
        }
    }
}
