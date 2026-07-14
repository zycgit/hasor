---
id: contenttype
sidebar_position: 2
title: b. ContentType
description: Set response ContentType values with @Produces.
---

# ContentType

For a response whose result is HTML, set `ContentType` with the `@Produces` annotation, as shown below:

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
If `@Produces` is not specified, Hasor Web first tries to resolve `ContentType` from the suffix of `renderTo` through the MIME mapping. If no MIME type matches, it does not actively set `ContentType`.
:::
