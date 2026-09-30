/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheControlTest {
    @Test
    public void standardPoliciesHaveDistinctSemantics() {
        assertNull(CacheControl.empty().getHeaderValue());
        assertEquals("", CacheControl.empty().toString());
        assertEquals("no-cache", CacheControl.noCache().getHeaderValue());
        assertEquals("no-store", CacheControl.noStore().getHeaderValue());
        assertEquals("max-age=0", CacheControl.maxAge(Duration.ZERO).getHeaderValue());
    }

    @Test
    public void versionedAssetsCanUseLongLivedPublicCaching() {
        CacheControl policy = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable().noTransform();
        assertEquals("max-age=31536000, public, immutable, no-transform", policy.getHeaderValue());
        assertEquals(policy.getHeaderValue(), policy.toString());
        assertEquals("max-age=3600, must-revalidate", CacheControl.maxAge(Duration.ofHours(1)).mustRevalidate().getHeaderValue());
    }

    @Test
    public void sharedCacheAndStaleWindowsUseWholeSeconds() {
        CacheControl policy = CacheControl.maxAge(Duration.ofMillis(1999)).sMaxAge(Duration.ofMinutes(5))
                .proxyRevalidate().staleWhileRevalidate(30, TimeUnit.SECONDS).staleIfError(Duration.ofHours(1));
        assertEquals("max-age=1, s-maxage=300, proxy-revalidate, stale-while-revalidate=30, stale-if-error=3600", policy.getHeaderValue());

        policy.sMaxAge(2, TimeUnit.MINUTES).staleWhileRevalidate(Duration.ofMinutes(1)).staleIfError(2, TimeUnit.HOURS);
        assertEquals("max-age=1, s-maxage=120, proxy-revalidate, stale-while-revalidate=60, stale-if-error=7200", policy.getHeaderValue());
    }

    @Test
    public void repeatedAndOppositeVisibilityDirectivesDoNotAccumulate() {
        CacheControl policy = CacheControl.noCache().cachePublic().cachePrivate().cachePrivate().mustRevalidate().mustRevalidate();
        assertEquals("no-cache, private, must-revalidate", policy.getHeaderValue());
        assertEquals("no-cache, must-revalidate, public", policy.cachePublic().getHeaderValue());
    }

    @Test
    public void invalidDurationsAreRejectedBeforeTruncationToSeconds() {
        this.rejects(() -> CacheControl.maxAge(-1, TimeUnit.MILLISECONDS));
        this.rejects(() -> CacheControl.maxAge(Duration.ofNanos(-1)));
        this.rejects(() -> CacheControl.empty().sMaxAge(Duration.ofSeconds(-1)));
        this.rejects(() -> CacheControl.empty().staleWhileRevalidate(-1, TimeUnit.SECONDS));
        this.rejects(() -> CacheControl.empty().staleIfError(Duration.ofSeconds(-1)));
        this.rejects(() -> CacheControl.maxAge(null));
        this.rejects(() -> CacheControl.maxAge(1, null));
    }

    private void rejects(Runnable configuration) {
        try {
            configuration.run();
            fail("Invalid cache duration must fail during configuration");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("non-negative"));
        }
    }
}
