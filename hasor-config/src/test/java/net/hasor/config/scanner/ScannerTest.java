/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.config.Configuration;
import net.hasor.core.ApiBinder;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.annotation.MappingToGroup;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ScannerTest {
    @Configuration
    @MappingTo("/first")
    @MappingTo("/second")
    public static class Both {
    }

    @Configuration
    public static class OnlyConfiguration {
    }

    @Test
    public void oneTraversalDispatchesAllConfigurationsBeforeMappings() throws Throwable {
        ApiBinder binder = mock(ApiBinder.class);
        ClassPathResourceLoader resources = spy(new ClassPathResourceLoader(getClass().getClassLoader()));
        when(binder.getResourceLoader()).thenReturn(resources);
        when(binder.getClassLoader()).thenReturn(getClass().getClassLoader());
        List<String> events = new ArrayList<>();
        AnnotationProcessor<Class<?>> configurations = processor(events, "configuration", Configuration.class);
        AnnotationProcessor<Class<?>> mappings = processor(events, "mapping", MappingTo.class, MappingToGroup.class);

        new Scanner(configurations, mappings).scan(binder, new String[] { "net.hasor.config.scanner" });

        assertEquals(Arrays.asList("configuration:Both", "configuration:OnlyConfiguration", "mapping:Both"), events);
        verify(resources, times(1)).scanResources(eq(MatchType.Prefix), any(net.hasor.cobble.loader.Scanner.class), any(String[].class));
        verify(binder, never()).findClass(any(), any(String[].class));
    }

    @SafeVarargs
    private final AnnotationProcessor<Class<?>> processor(List<String> events, String phase, Class<? extends Annotation>... annotations) {
        return new AnnotationProcessor<Class<?>>() {
            public List<Class<? extends Annotation>> annotationTypes() {
                return List.of(annotations);
            }

            public void process(ApiBinder binder, List<Class<?>> types) {
                for (Class<?> type : types) {
                    events.add(phase + ":" + type.getSimpleName());
                }
            }
        };
    }
}
