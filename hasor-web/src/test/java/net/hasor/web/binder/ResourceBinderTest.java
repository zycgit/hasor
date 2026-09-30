/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.time.Duration;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.web.CacheControl;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceBinderTest {
    @Test
    public void definitionIsAnIndependentConfigurationSnapshot() {
        ResourceLoader loader = mock(ResourceLoader.class);
        InnerResourceBinder binder = new InnerResourceBinder("/app/**", loader);
        binder.fallbackPaths("/app/tasks/*").excludedPrefixes("/app/private");
        ResourceDef definition = binder.build();
        binder.welcomeFile("other.html").fallbackPaths("/other/*").excludedPrefixes("/other");
        definition.fallbackPaths()[0] = "/changed";
        definition.excludedPrefixes()[0] = "/changed";
        assertSame(loader, definition.loader());
        assertEquals("/app", definition.pathPattern());
        assertEquals("index.html", definition.welcomeFile());
        assertArrayEquals(new String[] { "/app/tasks/*" }, definition.fallbackPaths());
        assertArrayEquals(new String[] { "/app/private" }, definition.excludedPrefixes());
    }

    @Test
    public void cachePolicyIsCapturedWhenRegistered() {
        InnerResourceBinder binder = new InnerResourceBinder("/assets/**", mock(ResourceLoader.class));
        assertEquals("no-cache", binder.build().cacheControl());
        CacheControl policy = CacheControl.maxAge(Duration.ofHours(1)).cachePublic();
        assertSame(binder, binder.cacheControl(policy));
        ResourceDef definition = binder.build();
        policy.cachePrivate();
        assertEquals("max-age=3600, public", binder.build().cacheControl());
        binder.cacheControl(CacheControl.noStore());
        assertEquals("max-age=3600, public", definition.cacheControl());
        assertEquals("no-store", binder.build().cacheControl());
        binder.cacheControl(CacheControl.empty());
        assertNull(binder.build().cacheControl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPolicyMustBeExplicitlyReplacedWithEmpty() {
        new InnerResourceBinder("/assets/**", mock(ResourceLoader.class)).cacheControl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void customPolicyCannotInjectResponseHeaders() {
        CacheControl policy = mock(CacheControl.class);
        when(policy.getHeaderValue()).thenReturn("no-cache\r\nX-Injected: value");
        new InnerResourceBinder("/assets/**", mock(ResourceLoader.class)).cacheControl(policy);
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
