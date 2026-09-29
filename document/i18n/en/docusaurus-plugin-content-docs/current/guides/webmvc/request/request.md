---
id: request
sidebar_position: 1
title: 4.2 Receiving Web Requests
description: Receive web requests with Hasor Web.
---

# 4.2 Receiving Web Requests

The simplest form for receiving a web request is shown below:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute() {
        ...
    }
}
```

`@MappingTo` declares the request path. With the class-level mapping above, the target method needs an HTTP method annotation such as `@Any`, `@Get`, or `@Post`. `@Any` accepts any HTTP method.

You can also place `@MappingTo` directly on individual methods. The framework registers each path separately:

```java
public class UserController {
    @Get
    @MappingTo("/users/{id}")
    public String find(@PathParameter("id") String id) {
        return id;
    }

    @Post
    @MappingTo("/users")
    public String create() {
        return "created";
    }
}
```

- Mapped methods must be public instance methods. A method-level `@MappingTo` without an HTTP method annotation accepts any HTTP method.
- When both the class and its methods declare `@MappingTo`, the class path is a shared prefix: `/users` on the class and `/{id}` on a method produce `/users/{id}`. Only methods with their own mappings are exposed in this form.
- Multiple paths and repeated `@MappingTo` annotations are supported. Different HTTP methods may share a path; duplicate path and HTTP method combinations fail during registration.
- Automatic scanning in `hasor-config` also discovers controllers with only method-level mappings. No class-level route or custom request dispatcher is required.

Mapping paths must start with `/`; `@MappingTo("/")` handles the application root. Register Controllers explicitly through `WebApiBinder.loadMappingTo`, or configure bounded scanning with [hasor-config](../../core/conf/java-config.md).
