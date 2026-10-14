---
id: contenttype
sidebar_position: 2
title: 4.4.2 ContentType
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.4.2 ContentType

需要覆盖渲染器默认响应类型时，可以使用 `@Produces` 注解。如下：

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
Hasor Web 会保留已经通过 `@Produces`、请求路径后缀或响应 API 写入响应的类型。响应尚未设置时，
再读取 `Invoker.contentType(...)`、根据渲染器名称查找 MIME，并使用
`RenderEngine.defaultContentType()` 的声明。内置 JSON 和文本渲染器分别声明
`application/json` 和 `text/plain`。普通返回值仍无法确定类型时使用 `application/octet-stream`；
模板不存在并交还 Servlet 容器时不提前设置兜底类型。
:::
