---
id: async
sidebar_position: 4
title: 4.8.4 Asynchronous Requests (Servlet 3.0)
description: Use Servlet 3.0 asynchronous requests in Hasor Web.
---

# 4.8.4 Asynchronous Requests (Servlet 3.0)

Before Servlet 3.0, Servlets used a thread-per-request model: every HTTP request was handled from start to finish by one thread. If a request needed I/O, such as accessing a database or calling a third-party service API, the corresponding thread synchronously waited for that I/O operation to finish. I/O operations are very slow, so the thread could not be returned to the thread pool in time for later use. As concurrency increased, this caused serious performance problems.

Even higher-level frameworks such as Spring and Struts could not escape this constraint because they were built on top of Servlet. To solve this problem, Servlet 3.0 introduced asynchronous processing, and Servlet 3.1 later introduced non-blocking I/O to further improve asynchronous-processing performance.

Hasor automatically detects the Servlet version of the container. With automatic detection, Hasor can be compatible with both Servlet 2.x and Servlet 3.x standards, allowing it to work on both Servlet platforms.

If you want to use Servlet 3.0 asynchronous requests, first make sure your web container supports Servlet 3.0. Otherwise, asynchronous requests are handled as ordinary requests.

Then mark the class with `@Async` as shown below. In a Servlet 3.0 container, Hasor automatically starts asynchronous processing through `javax.servlet.AsyncContext.start`.

MVC interceptors, the Action, and exception handlers run on the worker thread. Requests in Servlet asynchronous mode currently skip automatic return-value rendering; the worker must write the response explicitly.
When configuring `RuntimeFilter` manually, enable asynchronous support and include the `ASYNC` dispatcher mapping. Hasor Boot already configures this for all three embedded containers.
`afterCompletion` marks the end of the current MVC invocation without waiting for Servlet asynchronous completion. Application-managed asynchronous work should use an `AsyncListener` for completion, errors, and timeouts.
Propagation of unresolved asynchronous failures is currently incomplete and must not be relied on to produce an HTTP error response automatically. Handle failures and write the response within asynchronous work; an `ExceptionHandler` that writes the response must still return a non-null value to mark the exception as resolved.

```java title='Example'
@Async
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@RequestParameter("name") String userName,
                        @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

Or mark the method:

```java title='Configure the interceptor'
@MappingTo("/helloAction.do")
public class HelloAction {
    @Async
    @Any
    public void execute(@RequestParameter("name") String userName,
                        @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

## Asynchronous task completion

Starting with **5.3.0**, `AsyncInvocationWorker` separates work, error handling, and completion:

- When `doWork(Method)` returns normally, `finish(true)` calls `AsyncContext.complete()`.
- If work throws, `doWorkWhenError(Method, Throwable)` runs first. The framework implementation records the failure in the invocation's `Future`; `finish(false)` then calls `AsyncContext.dispatch()`.
- Custom error callbacks only handle or record failures; they must not call `complete()` or `dispatch()` again. Override `finish(boolean)` to change the completion strategy.

Here, `dispatch()` only redispatches the request. `RuntimeFilter` does not yet fully connect the original asynchronous failure to container error handling, so redispatch does not guarantee an HTTP 500 response or error body. Applications must still handle asynchronous responses explicitly as described above.
