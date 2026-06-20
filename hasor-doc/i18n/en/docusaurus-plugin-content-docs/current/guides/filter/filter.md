---
id: filter
sidebar_position: 1
title: Request Interceptors
description: Intercept requests in Hasor Web.
---

# Request Interceptors

Hasor provides three different ways to implement request interception:
- Intercept requests through the `InvokerFilter` interface (recommended).
- Intercept requests through the `javax.servlet.Filter` interface.
- Implement request interceptors through AOP.

The `InvokerFilter` and `Filter` interfaces work in an equivalent way and follow the same principle; they simply use different interfaces.
