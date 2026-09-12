---
id: requesttype
sidebar_position: 4
title: c. Distinguishing Request Types
description: Route requests by HTTP method in Hasor Web.
---

# Distinguishing Request Types

In browser developer tools, the request Headers panel identifies the HTTP method:

| Header field | Example |
| --- | --- |
| Request URL | http://www.hasor.net/account/login.htm |
| Request Method | **GET** |
| Status Code | 200 |

The **Request Method** field is the request type used by the annotations below.

If one method should receive every request type, annotate it with `@Any`. To distinguish request types, use different methods for POST and GET as shown below:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Post
    public void doPost() {
        ...
    }
    @Get
    public void doGet() {
        ...
    }
}
```

Hasor provides the following request-type annotations by default:

| Annotation | Meaning                    |
|------------|----------------------------|
| `@Any`     | Any request type           |
| `@Get`     | GET request                |
| `@Post`    | POST request               |
| `@Put`     | PUT request                |
| `@Head`    | HEAD request               |
| `@Options` | OPTION request             |

:::tip
Hasor supports marking a handler with multiple request-type annotations at the same time, such as using both `@Get` and `@Post`.
:::
