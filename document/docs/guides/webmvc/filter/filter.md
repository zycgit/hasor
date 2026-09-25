---
id: filter
sidebar_position: 1
title: 4.6 请求拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.6 请求拦截器

Hasor 的请求处理分为 HTTP 过滤器和 MVC 拦截器两个层次：

- 通过 `WebApiBinder.filter(...)` 或 `jeeFilter(...)` 注册的 `InvokerFilter` 和 `javax.servlet.Filter`，按路径匹配并包裹已匹配 Action 的 MVC 流程。
- [`HandlerInterceptor`](./interceptor.md) 位于 MVC 内部，只处理已匹配 Action 的请求，通过 `WebApiBinder.bindInterceptor(拦截器)` 注册。

MVC 前置处理按注册调用顺序执行；用户在 `WebMvcConfigurer` 实现中决定注册顺序，后置处理和完成回调逆序执行。
MVC 内部异常由 [`ExceptionHandler`](../response/exception.md) 处理；过滤器自身、静态资源和后续 Servlet 的异常留在外层。
AOP 仍可用于方法级拦截，但不替代 HTTP 或 MVC 的请求生命周期。
静态资源和未匹配 Action 的请求不会进入上述 Hasor 过滤器链；需要统一拦截这些请求时，应在宿主 Servlet 容器中注册 Filter。
