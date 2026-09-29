/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.List;
import javax.servlet.ServletContext;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.methodscan.MethodOnlyController;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.binder.MappingDef;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodMappingScanTest {
    @Test
    public void scansMethodOnlyControllersAndInjectsTheirDependencies() throws Exception {
        try (AppContext context = this.application("net.hasor.config.methodscan").build()) {
            List<MappingDef> mappings = context.findBindingBean(MappingDef.class);
            assertEquals(4, mappings.size());
            assertEquals(2, mappings.stream().filter(mapping -> mapping.getMappingTo().equals("/shared")).count());
            assertEquals("hello:", context.getInstance(MethodOnlyController.class).read());
            assertTrue(mappings.stream().anyMatch(mapping -> mapping.getMappingTo().equals("/alias/{id}")));
        }
    }

    private Hasor application(String scope) {
        ServletContext servlet = mock(ServletContext.class);
        when(servlet.getClassLoader()).thenReturn(this.getClass().getClassLoader());
        when(servlet.getContextPath()).thenReturn("");
        when(servlet.getEffectiveMajorVersion()).thenReturn(3);
        when(servlet.getVirtualServerName()).thenReturn("test");
        return Hasor.create(servlet).registerShutdownHook(false).addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", scope);
    }

    @Test
    public void conflictingMethodsWithDifferentPathVariableNamesFailStartup() {
        try (AppContext ignored = this.application("net.hasor.config.methodconflict").build()) {
            fail("Conflicting method routes must fail");
        } catch (Exception failure) {
            Throwable cause = failure;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            assertTrue(cause.getMessage(), cause.getMessage().contains("Conflicting auto-scanned route"));
        }
    }
}