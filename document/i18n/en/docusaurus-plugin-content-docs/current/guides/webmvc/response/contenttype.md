---
id: contenttype
sidebar_position: 2
title: 4.4.2 ContentType
description: Set response ContentType values with @Produces.
---

# 4.4.2 ContentType

Use `@Produces` when a handler needs to override the renderer's default response type:

```java
@MappingTo("/my.html")
public class HtmlProduces {
    @Any
    @Produces("text/html")
    public void testProduces1(RenderInvoker invoker) {
        invoker.renderTo("flt", "/my.flt");
    }
}
```

:::tip
Hasor Web preserves a type already set through `@Produces`, a request-path suffix, or the response API.
Otherwise it checks `Invoker.contentType(...)`, MIME inferred from the renderer name, and
`RenderEngine.defaultContentType()`. Built-in JSON and text renderers declare `application/json` and
`text/plain`. A normal return value with no resolvable type falls back to `application/octet-stream`.
:::
