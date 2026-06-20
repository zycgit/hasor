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
    @Produces("test/html")
    public void testProduces1() {
        invoker.renderTo("flt", "/my.flt");
    }
}
```

:::tip
If the `@Produces` annotation is not specified, Hasor does not actively set `ContentType`.
:::
