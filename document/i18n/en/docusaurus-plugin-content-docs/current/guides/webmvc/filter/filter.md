---
id: filter
sidebar_position: 1
title: 4.6 Request Interceptors
description: Intercept requests in Hasor Web.
---

# 4.6 Request Interceptors

Hasor separates outer HTTP filters from MVC interceptors:

- `InvokerFilter` and `javax.servlet.Filter` registered through `WebApiBinder.filter(...)` or `jeeFilter(...)` match paths and surround the MVC pipeline for matched Actions.
- [`HandlerInterceptor`](./interceptor.md) runs inside MVC for matched Actions. Register it with `WebApiBinder.bindInterceptor(interceptor)`.

MVC pre-handlers run in registration order, as arranged by the application in its `WebMvcConfigurer` implementation. Post-handlers and completion callbacks run in reverse order.
[`ExceptionHandler`](../response/exception.md) resolves MVC failures; failures in filters, static resources, or downstream Servlets remain outside MVC.
AOP remains available for method interception but does not replace the HTTP or MVC lifecycle.
Static resources and requests without a matched Action bypass this Hasor filter chain. Register a Filter with the host Servlet container when those requests also need interception.
