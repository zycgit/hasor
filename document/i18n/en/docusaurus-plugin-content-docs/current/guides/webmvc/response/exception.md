---
id: exception
sidebar_position: 6
title: 4.4.6 Exception Handling
description: Typed ExceptionHandler registration and managed configuration factories.
---

# 4.4.6 Exception Handling

The features in this section are available starting with **5.3.0**.

`hasor-web` provides `net.hasor.web.ExceptionHandler<E extends Throwable>`. It receives the current `Invoker` and exception and returns response data.
Register handlers through `WebApiBinder.addExceptionHandler(...)`:

```java
binder.addExceptionHandler(IllegalArgumentException.class, (invoker, error) -> {
    invoker.getHttpResponse().setStatus(400);
    return Map.of("message", "Invalid request");
});
binder.addExceptionHandler(RuntimeException.class, (invoker, error) -> {
    invoker.getHttpResponse().setStatus(500);
    return Map.of("message", "Request failed");
});
```

Matching starts at the thrown exception's class and walks its superclasses. The closest registered type wins, regardless of registration order.
In this example, `IllegalArgumentException` selects the first handler. With no match, the original exception propagates.
Registering the same exception type twice fails during startup.
The Web context loads exception handler registration definitions at initialization; requests match types against these definitions directly.

- A non-null value becomes the final invocation result. The [rendering rules](./defaults.md) and `SkipRender` state then determine whether it is written. Objects default to JSON and strings to text; `false`, zero, and empty strings are valid results. A void Action follows the same rule.
- A null result rethrows the original exception without trying a broader handler, even if the handler has already written to the response or called `setSkipRender()`.
- A handler failure propagates with the original exception attached as suppressed; handlers are not invoked recursively.

Handlers set the HTTP status and choose the application's public error payload.
A handler that writes its own response must still return a non-null value, such as an empty string, to resolve the exception; no body is appended. An empty string can also accompany a response containing only status and headers.
A handler can call `invoker.setSkipRender()` to skip rendering its return value, but must still return a non-null value to resolve the exception.
`setSkipRender()` only controls rendering. It does not bypass exception handlers or suppress unresolved failures.
If the response was committed or its Writer/output stream was acquired before the failure, no exception response is attempted; the failure propagates outward.
`InvokerCaller` coordinates failures from MVC `preHandle`, the Controller, and `postHandle`.
After invocation and exception handling finish, `isSkipRender()` determines whether to render the final result. Rendering failures propagate without invoking exception handlers again. If rendering an exception handler's result fails, the original invocation failure is retained as a suppressed exception.
Entered interceptors receive `afterCompletion(invoker, null)` when invocation succeeds or an exception is resolved, provided subsequent rendering succeeds or is skipped. A final failure in invocation, the exception handler, or rendering is passed to completion callbacks.
Completion callback errors are logged without replacing the outcome or invoking another exception handler.
HTTP filters surround MVC. Their own failures, static resource failures, and downstream Servlet failures stay outside MVC exception handling; an outer filter can catch unresolved MVC failures.
A `void` return type or `@Async` annotation does not change the exception contract: failures propagate when no handler matches or the selected handler returns `null`.
Framework `@Async` Actions invoke exception handlers on the worker thread. Response output and unresolved failure propagation have separate limitations in asynchronous mode; see [asynchronous requests](../j2ee/async.md). Application-managed asynchronous tasks handle their own failures.

## Configuration factories

With `hasor-config`, declare an `@Exception` factory in a `@Configuration` class.
The annotation is `net.hasor.config.web.Exception`; the method returns an `ExceptionHandler`, and its parameters are injected from the container:

```java
import java.io.IOException;
import java.util.Map;
import net.hasor.config.Configuration;
import net.hasor.config.web.Exception;
import net.hasor.web.ExceptionHandler;

@Configuration
public class ErrorConfiguration {
    @Exception({IOException.class, IllegalStateException.class})
    public ExceptionHandler<Throwable> requestErrors() {
        return (invoker, error) -> {
            invoker.getHttpResponse().setStatus(500);
            return Map.of("message", "Request failed");
        };
    }
}
```

Discover the configuration through `hasor.loadPackages` or load it explicitly with `ConfigurationModule.of(ErrorConfiguration.class)`.
Factories default to singleton scope and use their method name as the bean ID. Multiple exception types share the same instance.
Factory arguments and configuration fields use the existing dependency injection mechanism. Factories are not invoked during module registration.
`@Exception` alone registers the factory. When combined with `@Bean`, it reuses the existing Bean binding instead of registering another factory.
Add `@Bean(value = "hostErrors", initMethod = "init", destroyMethod = "destroy")` for naming and lifecycle options, use `singleton = false` for prototype scope, or declare `@DependsOn` for explicit initialization dependencies.
The returned handler must accept every declared exception type.
Factories follow the [Bean method constraints](../../core/conf/java-config.md): methods cannot be static or abstract, and the created handler must not be null. `@Exception` must declare at least one exception type and requires a Web application.
Annotated and manual registrations use the same registry and matching rules.
