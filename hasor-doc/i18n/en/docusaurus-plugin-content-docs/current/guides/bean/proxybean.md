---
id: proxybean
sidebar_position: 4
title: c.委托创建Bean
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 委托创建Bean

Bean 委托创建能力是在 4.2.1 版本之后才提供的，利用 `TypeSupplier` 可以将注册到 Hasor 中到 Bean 委托给其它 IoC 容器来创建。

在此之前 Hasor 的 Bean 创建只有工厂方式。如下：

```java
public class TypeBean1Factory implements Supplier<TypeBean1> {
    private TypeBean1 target = new TypeBean1();

    public TypeBean1 get() {
        return target;//工厂方式创建 TypeBean1
    }
}

public class TypeBean2Factory implements Supplier<TypeBean2> {
    private TypeBean2 target = new TypeBean2();

    public TypeBean2 get() {
        return target;//工厂方式创建 TypeBean2
    }
}

AppContext appContext = Hasor.create().build(apiBinder -> {
    // .创建工厂
    TypeBean1Factory factory1 = new TypeBean1Factory();
    TypeBean2Factory factory2 = new TypeBean2Factory();
    // .注册 Bean，并指明工厂
    apiBinder.bindType(TypeBean1.class).toProvider(factory1);
    apiBinder.bindType(TypeBean2.class).toProvider(factory2);
});

// .工厂方式创建 Bean
TypeBean1 typeBean1 = appContext.getInstance(TypeBean1.class);
TypeBean2 typeBean2 = appContext.getInstance(TypeBean2.class);
```