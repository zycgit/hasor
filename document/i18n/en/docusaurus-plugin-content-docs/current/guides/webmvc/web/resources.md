---
id: resources
sidebar_position: 2
title: 4.1.1 Static Resources and Frontend Applications
description: Configure resource sources and frontend route fallbacks with ResourceBinder.
---

# 4.1.1 Static Resources and Frontend Applications

Resource handling is built into Hasor Web and does not require Config or Boot.

```java
binder.addResource("/app/**", resourceLoader)
      .welcomeFile("index.html")
      .fallbackPaths("/app/tasks/*")
      .excludedPrefixes("/app/api")
      .cacheControl("no-cache");
```

The returned `net.hasor.web.binder.ResourceBinder` configures access policies. Supply at least one Cobble ResourceLoader.
Combine multiple sources for one address: `binder.addResource("/assets/**", localLoader, classpathLoader)` searches them in order.
Use this approach for source fallback rather than registering the same mapping repeatedly.

Mappings use prefixes and may end in `/*` or `/**`. Lower `order` values take precedence; ties prefer longer prefixes, then declaration order.
The default welcome file is index.html, with no extension restriction; null disables it. SPA fallback must be explicitly configured and does not mask missing resources with filename extensions.
GET, HEAD, and Last-Modified/304 validation are supported. The default cache policy is no-cache.

## Interaction with Actions and filters

Actions always take precedence; resources are tried only if no Action matches. A matched resource rule with a missing file returns 404 without trying other rules. The Servlet chain continues only if no rule matches.
Resources bypass filters registered through `WebApiBinder.filter(...)` or `jeeFilter(...)`, as well as MVC `HandlerInterceptor`, MVC exception handling, and return-value rendering.
Register a Filter with the host Servlet container to apply authentication, auditing, or CORS to resources.

## Integration

`WebMvcConfigurer.addResourceHandlers(WebApiBinder)` uses the same API.
Boot assembles META-INF/resources by default, including resources in dependency JARs. SPA fallback is disabled by default.
CORS is an optional Config feature; its filter is `net.hasor.config.web.cors.CorsFilter`. Adding Web does not automatically enable cross-origin policies.
The old Registration, Registry, and addResourceHandler interfaces are replaced by addResource and ResourceBinder.
