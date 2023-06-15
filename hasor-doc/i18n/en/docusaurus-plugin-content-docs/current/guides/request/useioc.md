---
id: useioc
sidebar_position: 2
title: a.使用IoC
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 使用IoC

请求处理的类的属性可以被依赖注入：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Inject
    private PayService payService

    public void execute() {
        ...
    }
}
```

:::tip
具体依赖注入部分内容可以在【依赖注入(IoC)】查阅。
:::