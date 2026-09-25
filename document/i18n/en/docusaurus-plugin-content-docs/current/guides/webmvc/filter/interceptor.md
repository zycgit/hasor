---
id: interceptor
sidebar_position: 4
title: 4.6.3 MVC Interceptors
description: Configure HandlerInterceptor instances in registration order through WebMvcConfigurer.
---

# 4.6.3 MVC Interceptors

The features in this section are available starting with **5.3.0**.

`net.hasor.web.HandlerInterceptor` applies only to MVC requests with a matched Action.
HTTP filters surround MVC. Static resources and requests delegated to downstream Servlets bypass MVC interceptors.

## Registration and order

```java
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(WebApiBinder binder) {
        binder.bindInterceptor(new LoginInterceptor());
        binder.bindInterceptor(new AuditInterceptor());
    }
}
```

`WebApiBinder.bindInterceptor(HandlerInterceptor interceptor)` runs pre-handlers in registration order.
The application chooses the sequence of registration calls in its `WebMvcConfigurer` implementation, and the framework uses that sequence directly.
Login enters before Audit in this example; post-handlers and completion callbacks run in reverse order.
An interceptor instance is shared between requests. Store request-specific state in the `Invoker` or request attributes.
Complete registration during module configuration. The Web context loads the interceptor list at initialization and reuses it for requests without repeated container lookups.

With `hasor-config`, use the same API inside `WebMvcConfigurer.addInterceptors(WebApiBinder)`.

## Lifecycle

```java
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(Invoker invoker) {
        if (invoker.getHttpRequest().getUserPrincipal() == null) {
            invoker.getHttpResponse().setStatus(401);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(Invoker invoker, Object result) {
        // Inspect or replace Invoker.RETURN_DATA_KEY before rendering.
    }

    @Override
    public void afterCompletion(Invoker invoker, Throwable failure) {
        // Release resources acquired by this interceptor.
    }
}
```

- `preHandle` runs before the Controller in registration order. Returning `false` means the request is handled and stops later interceptors, the Controller, and automatic rendering.
- `postHandle` runs in reverse order after a successful Controller call, before rendering. Replace the result with `invoker.put(Invoker.RETURN_DATA_KEY, result)` if needed.
- `afterCompletion` runs in reverse order after rendering or termination, only for interceptors whose `preHandle` returned `true`. An interceptor that returned `false` or threw from `preHandle` does not receive its own completion callback.

A normal request follows: outer Filter → pre-handlers → Controller → reverse post-handlers → rendering → reverse completion callbacks → return to outer Filter.
Failures in `preHandle`, the Controller, or `postHandle` enter [MVC exception handling](../response/exception.md). Rendering runs after invocation and exception handling finish; rendering failures propagate outward and are passed to `afterCompletion`.
Completion receives `null` when invocation succeeds or an exception is resolved and rendering succeeds or is skipped. Otherwise it receives the final failure propagated outward, including a rendering failure. Exception recovery does not rerun post-handlers.
Completion callback failures are logged; remaining completion callbacks still run.

## Manual responses and asynchronous work

Acquiring the response Writer or output stream suppresses additional automatic rendering.
A pre-handler that only sets status or headers can return `false` to stop MVC execution.
In `postHandle`, call `invoker.setSkipRender()` to skip further automatic rendering, including an explicitly selected renderer or view.
The MVC pipeline checks this state before output; renderers do not check it internally.
This does not stop remaining interceptor callbacks; a pre-handler must still return `false` to stop MVC execution.
Query the state with `invoker.isSkipRender()`. The state belongs to the current request; once rendering is skipped, it cannot be re-enabled.

Framework `@Async` Actions run MVC callbacks on a worker thread; see [asynchronous requests](../j2ee/async.md) for response output limitations.
When an Action calls Servlet `startAsync()` itself, a normal return still runs `postHandle`, but asynchronous mode skips automatic rendering. `afterCompletion` runs when the current MVC invocation ends, without waiting for Servlet asynchronous completion. Use a Servlet `AsyncListener` for asynchronous completion, error, and timeout events.
Application-managed asynchronous tasks execute outside the MVC exception-handling stack.
