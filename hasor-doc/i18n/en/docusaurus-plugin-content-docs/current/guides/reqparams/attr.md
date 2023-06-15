---
id: attr
sidebar_position: 5
title: e.获取Attribute
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 获取Attribute

最原始的办法是通过 `httpRequest.getAttribute` 获取，但 Hasor 提供了 `@AttributeParameter` 注解

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute(@AttributeParameter("value") boolean value) {
        ...
    }
}
```
