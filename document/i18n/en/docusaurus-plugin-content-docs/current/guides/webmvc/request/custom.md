---
id: custom
sidebar_position: 6
title: d. Custom Requests
description: Define custom HTTP method annotations for Hasor Web.
---

# Custom Requests

Requests are usually initiated by browsers, and request types are normally fixed. If you use an AJAX framework or send requests from outside a browser, the request type can actually be modified. Hasor supports custom request types.

For example, define a method call that receives request type `ABC`:

```java
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("ABC")
public @interface ABC {
}
```

Then specify it when receiving a request:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @ABC
    public void doAbc() {
        ...
    }

    @Get
    public void doGet() {
        ...
    }
}
```
