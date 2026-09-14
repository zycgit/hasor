---
id: contenttype
sidebar_position: 2
title: 4.4.2 ContentType
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.4.2 ContentType

作为 html 为结果的响应，设置 `ContentType` 需要通过 `@Produces` 注解。如下：

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
如果没有指定 `@Produces` 注解，Hasor Web 会优先根据 `renderTo` 的资源后缀从 MIME 映射中查找 `ContentType`。如果也匹配不到 MIME 类型，则不会主动设置 `ContentType`。
:::
