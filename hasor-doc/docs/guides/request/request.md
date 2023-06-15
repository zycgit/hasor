---
id: request
sidebar_position: 1
title: 接收Web请求
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 接收Web请求

接收Web请求，下面是最简形态：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute() {
        ...
    }
}
```
