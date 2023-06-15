---
id: initbean
sidebar_position: 2
title: a.初始化 Bean
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 初始化 Bean

有时候我们希望有 Bean 可以在被创建时自动调用一个 init 方法，本小节就来向大家展示一下 Hasor 这方面的能力。

## 方式一

通过 `net.hasor.core.Init` 或 `javax.annotation.PostConstruct` 注解，例如下面这样：

```java
public class PojoBean {
    @Init
    public void init(){
        ...
    }
}
```

方式二

```java
public class PojoBean {
    // 不使用注解，通过 apiBinder 来指定。
    public void init(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class).initMethod("init");
    }
}
```
