---
id: resources
sidebar_position: 2
title: 4.1.1 Static Resources and Frontend Applications
description: Configure resource sources and frontend route fallbacks with ResourceBinder.
---

# 4.1.1 Static Resources and Frontend Applications

Resource handling is built into Hasor Web and does not require Config or Boot.

```java
import net.hasor.web.CacheControl;

binder.addResource("/app/**", resourceLoader)
      .welcomeFile("index.html")
      .fallbackPaths("/app/tasks/*")
      .excludedPrefixes("/app/api")
      .cacheControl(CacheControl.noCache());
```

The returned `net.hasor.web.binder.ResourceBinder` configures access policies. Supply at least one Cobble ResourceLoader.
Combine multiple sources for one address: `binder.addResource("/assets/**", localLoader, classpathLoader)` searches them in order.
Use this approach for source fallback rather than registering the same mapping repeatedly.

Mappings use prefixes and may end in `/*` or `/**`. Lower `order` values take precedence; ties prefer longer prefixes, then declaration order.
The default welcome file is index.html, with no extension restriction; null disables it. SPA fallback must be explicitly configured and does not mask missing resources with filename extensions.
GET, HEAD, and Last-Modified/304 validation are supported. The default cache policy is no-cache.

## Cache policies

`cacheControl` accepts `net.hasor.web.CacheControl`. Factory methods and chained calls compose response directives using an API modeled on [Spring CacheControl](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/CacheControl.html).

```java
import java.time.Duration;
import net.hasor.web.CacheControl;

binder.addResource("/assets/**", resourceLoader)
      .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());

binder.addResource("/preview/**", previewLoader)
      .cacheControl(CacheControl.noStore());
```

- `noCache()` permits storage but requires validation before reuse.
- `noStore()` prevents response storage.
- `maxAge(Duration)` or `maxAge(long, TimeUnit)` sets freshness in seconds.
- `cachePublic()` and `cachePrivate()` select shared or private caching; the last selection replaces the previous one.
- `mustRevalidate()`, `proxyRevalidate()`, `noTransform()` and `immutable()` add their respective directives.
- `sMaxAge(...)`, `staleWhileRevalidate(...)` and `staleIfError(...)` configure shared freshness and stale response windows, using either time form above.
- `empty()` writes no additional Cache-Control header, preserving any host header.

The binder captures the header string when `cacheControl` is called. Later policy changes do not affect registered resources. Durations must be non-negative; fractional seconds are discarded. Omitting the configuration still uses `no-cache`. GET, HEAD and 304 responses use the same policy.

## Interaction with Actions and filters

Actions always take precedence; resources are tried only if no Action matches. A matched resource rule with a missing file returns 404 without trying other rules. The Servlet chain continues only if no rule matches.
Resources bypass filters registered through `WebApiBinder.filter(...)` or `jeeFilter(...)`, as well as MVC `HandlerInterceptor`, MVC exception handling, and return-value rendering.
Register a Filter with the host Servlet container to apply authentication, auditing, or CORS to resources.

## Integration

`WebMvcConfigurer.addResourceHandlers(WebApiBinder)` uses the same API.
Boot assembles META-INF/resources by default, including resources in dependency JARs. SPA fallback is disabled by default.
CORS is an optional Config feature; its filter is `net.hasor.config.web.cors.CorsFilter`. Adding Web does not automatically enable cross-origin policies.
The old Registration, Registry, and addResourceHandler interfaces are replaced by addResource and ResourceBinder.
