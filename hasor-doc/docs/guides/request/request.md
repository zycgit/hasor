---
id: request
sidebar_position: 1
title: 接收Web请求
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 接收Web请求

接收Web请求，下面是最简形态：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute() {
        ...
    }
}
```

`@MappingTo` 负责声明请求路径，方法上需要使用 `@Any`、`@Get`、`@Post` 等 HTTP 方法注解。上面的 `@Any` 表示接收任意 HTTP 方法。
