/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import net.hasor.cobble.loader.ResourceLoader;
import org.junit.Test;

public class ResourceBinderTest {
    @Test
    public void definitionIsAnIndependentConfigurationSnapshot() {
        ResourceLoader loader = org.mockito.Mockito.mock(ResourceLoader.class);
        InnerResourceBinder binder = new InnerResourceBinder("/app/**", loader);
        binder.fallbackPaths("/app/tasks/*").excludedPrefixes("/app/private");
        ResourceDef definition = binder.build();
        binder.welcomeFile("other.html").fallbackPaths("/other/*").excludedPrefixes("/other");
        definition.fallbackPaths()[0] = "/changed";
        definition.excludedPrefixes()[0] = "/changed";
        org.junit.Assert.assertSame(loader, definition.loader());
        org.junit.Assert.assertEquals("/app", definition.pathPattern());
        org.junit.Assert.assertEquals("index.html", definition.welcomeFile());
        org.junit.Assert.assertArrayEquals(new String[] { "/app/tasks/*" }, definition.fallbackPaths());
        org.junit.Assert.assertArrayEquals(new String[] { "/app/private" }, definition.excludedPrefixes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresLoaders() {
        new InnerResourceBinder("/**");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullArray() {
        new InnerResourceBinder("/**", (ResourceLoader[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullLoader() {
        new InnerResourceBinder("/**", (ResourceLoader) null);
    }
}
