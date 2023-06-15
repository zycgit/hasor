---
id: contenttype
sidebar_position: 2
title: b.ContentType
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# ContentType

作为 html 为结果的响应，设置 `ContentType` 需要通过 `@Produces` 注解。如下：

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

::;tip
如果没有指定 `@Produces` 注释，Hasor 也不会主动设置 `ContentType`。
:::
