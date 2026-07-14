---
id: async
sidebar_position: 4
title: d. Asynchronous Requests (Servlet 3.0)
description: Use Servlet 3.0 asynchronous requests in Hasor Web.
---

# Asynchronous Requests (Servlet 3.0)

Before Servlet 3.0, Servlets used a thread-per-request model: every HTTP request was handled from start to finish by one thread. If a request needed I/O, such as accessing a database or calling a third-party service API, the corresponding thread synchronously waited for that I/O operation to finish. I/O operations are very slow, so the thread could not be returned to the thread pool in time for later use. As concurrency increased, this caused serious performance problems.

Even higher-level frameworks such as Spring and Struts could not escape this constraint because they were built on top of Servlet. To solve this problem, Servlet 3.0 introduced asynchronous processing, and Servlet 3.1 later introduced non-blocking I/O to further improve asynchronous-processing performance.

Hasor automatically detects the Servlet version of the container. With automatic detection, Hasor can be compatible with both Servlet 2.x and Servlet 3.x standards, allowing it to work on both Servlet platforms.

If you want to use Servlet 3.0 asynchronous requests, first make sure your web container supports Servlet 3.0. Otherwise, asynchronous requests are handled as ordinary requests.

Then mark the class with `@Async` as shown below. In a Servlet 3.0 container, Hasor automatically starts asynchronous processing through `javax.servlet.AsyncContext.start`.

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
