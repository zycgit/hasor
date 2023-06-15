---
id: requesttype
sidebar_position: 4
title: c.区分请求类型
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 区分请求类型

如果你不知道什么是请求类型，那么请看这里：

![](../_img/CC2_11EF_EF69_F9BE.png)

Hasor默认是接收所有类型的请求，如果想区分请求类型。可以如下例子：用不同方法接收 POST 和 GET

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

Hasor 默认提供的请求类型注解有：

| 注解         | 含义            |
|------------|---------------|
| `@Any`     | 表示 任意 类型的请求   |
| `@Get`     | 表示 GET 请求     |
| `@Post`    | 表示 POST 请求    |
| `@Put`     | 表示 PUT 请求     |
| `@Head`    | 表示 HEAD 请求    |
| `@Options` | 表示 OPTION 类请求 |

:::tip
Hasor 中是支持同时标记多种请求类型处理标记的，例如：同时使用 @Get 和 @Post
:::