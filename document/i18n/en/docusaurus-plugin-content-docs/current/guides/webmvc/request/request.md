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

`@MappingTo` declares the request path, and the target method needs an HTTP method annotation such as `@Any`, `@Get`, or `@Post`. The `@Any` annotation above means the method accepts any HTTP method.

Mapping paths must start with `/`; `@MappingTo("/")` handles the application root. Register Controllers explicitly through `WebApiBinder.loadMappingTo`, or configure bounded scanning with [hasor-config](../../core/conf/java-config.md).
