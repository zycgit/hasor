---
id: useioc
sidebar_position: 2
title: 使用IoC
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 使用IoC

请求处理的类的属性可以被依赖注入：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Inject
    private PayService payService

    @Any
    public void execute() {
        ...
    }
}
```

:::tip
具体依赖注入部分内容可以在【依赖注入(IoC)】查阅。
:::
