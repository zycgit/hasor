/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/** Builds Cache-Control response directives. Configure an instance before registering it. */
public class CacheControl {
    private final Map<String, String> directives = new LinkedHashMap<>();

    protected CacheControl() {
    }

    /** An empty policy leaves the response's Cache-Control header unchanged. */
    public static CacheControl empty() {
        return new CacheControl();
    }

    /** Allows storage, but requires validation before reusing a cached response. */
    public static CacheControl noCache() {
        return new CacheControl().directive("no-cache");
    }

    private CacheControl directive(String name) {
        this.directives.put(name, null);
        return this;
    }

    /** Prevents browsers and shared caches from storing the response. */
    public static CacheControl noStore() {
        return new CacheControl().directive("no-store");
    }

    /** Sets the response freshness lifetime; fractional seconds are discarded. */
    public static CacheControl maxAge(Duration duration) {
        return new CacheControl().duration("max-age", duration);
    }

    public static CacheControl maxAge(long amount, TimeUnit unit) {
        return new CacheControl().duration("max-age", amount, unit);
    }

    private CacheControl duration(String name, Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException(name + " requires a non-negative duration");
        }
        this.directives.put(name, Long.toString(duration.getSeconds()));
        return this;
    }

    private CacheControl duration(String name, long amount, TimeUnit unit) {
        if (amount < 0 || unit == null) {
            throw new IllegalArgumentException(name + " requires a non-negative amount and a time unit");
        }
        return this.duration(name, Duration.ofSeconds(unit.toSeconds(amount)));
    }

    /** Requires successful validation before serving a stale response. */
    public CacheControl mustRevalidate() {
        return this.directive("must-revalidate");
    }

    /** Prevents intermediaries from transforming the response content. */
    public CacheControl noTransform() {
        return this.directive("no-transform");
    }

    /** Allows shared caching; replaces a previous private directive. */
    public CacheControl cachePublic() {
        this.directives.remove("private");
        return this.directive("public");
    }

    /** Restricts storage to private caches; replaces a previous public directive. */
    public CacheControl cachePrivate() {
        this.directives.remove("public");
        return this.directive("private");
    }

    /** Requires shared caches to validate stale responses. */
    public CacheControl proxyRevalidate() {
        return this.directive("proxy-revalidate");
    }

    /** Declares that the representation will not change during its freshness lifetime. */
    public CacheControl immutable() {
        return this.directive("immutable");
    }

    /** Overrides max-age for shared caches. */
    public CacheControl sMaxAge(Duration duration) {
        return this.duration("s-maxage", duration);
    }

    public CacheControl sMaxAge(long amount, TimeUnit unit) {
        return this.duration("s-maxage", amount, unit);
    }

    /** Allows a stale response while the cache refreshes it in the background. */
    public CacheControl staleWhileRevalidate(Duration duration) {
        return this.duration("stale-while-revalidate", duration);
    }

    public CacheControl staleWhileRevalidate(long amount, TimeUnit unit) {
        return this.duration("stale-while-revalidate", amount, unit);
    }

    /** Allows a stale response when refreshing it fails. */
    public CacheControl staleIfError(Duration duration) {
        return this.duration("stale-if-error", duration);
    }

    public CacheControl staleIfError(long amount, TimeUnit unit) {
        return this.duration("stale-if-error", amount, unit);
    }

    /** Returns directives in configuration order, or null if none were configured. */
    public String getHeaderValue() {
        if (this.directives.isEmpty()) {
            return null;
        }
        StringJoiner header = new StringJoiner(", ");
        this.directives.forEach((name, value) -> header.add(value == null ? name : name + "=" + value));
        return header.toString();
    }

    @Override
    public String toString() {
        String value = this.getHeaderValue();
        return value == null ? "" : value;
    }
}
