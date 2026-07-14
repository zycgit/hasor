---
id: attr
sidebar_position: 5
title: e.获取Attribute
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 获取Attribute

最原始的办法是通过 `httpRequest.getAttribute` 获取，但 Hasor 提供了 `@AttributeParameter` 注解

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@AttributeParameter("value") boolean value) {
        ...
    }
}
```
