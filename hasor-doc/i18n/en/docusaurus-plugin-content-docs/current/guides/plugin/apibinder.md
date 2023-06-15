---
id: apibinder
sidebar_position: 1
title: a.ApiBinder
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# ApiBinder

:::tip
ApiBinder 扩展机制是从 Hasor 2.3 之后加入的。这个扩展机制可以帮助应用或工具框架在 init 阶段构建自己的交互接口。

它存在最大的意义在于可以统一开发体验，即基于 ApiBinder 的扩展的程序，其加载和初始化方式可以融合在 Module 之中。

这种能力使得扩展工具即便是第三方工具，使用者在使用的时候感受犹如 Hasor 原生一般。
:::

## 原理

在 Hasor init 过程的 newApiBinder 阶段，Hasor 会从配置文件中收集所有 ApiBinder 扩展点并创建它们。

![](../_img/CC2_E1VA_864B_GCI5.png)

被创建的扩展点对象会存放在一个叫 supportMap 的 Map 对象中，Map 的 key 是用户自定义的 ApiBinder 接口。

最后这些扩展点类型会通过 Java 的动态代理机制归纳到一个代理对象身上，然后每次方法调用会自动路由到对应的 Api 提供者中。

## 例子

在下面例子中这个类型是 `net.test.binder.TestBinder`。首先在 Hasor 的配置文件中注册一个 ApiBinder 扩展。其中 `TestBinderCreator` 类实现了 `net.hasor.core.binder.ApiBinderCreator` 接口。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.apiBinderSet>
        <!-- 注册扩展 -->
        <binder type="net.test.binder.TestBinder">net.test.binder.TestBinderCreator</binder>
    </hasor.apiBinderSet>
</config>
```

TestBinderCreator 实现如下：

```java
public interface TestBinder extends ApiBinder {
    public void hello();
}

public class TestBinderImpl extends ApiBinderWrap implements TestBinder {
    public TestBinderImpl(ApiBinder apiBinder) {
        super(apiBinder);
    }

    public void hello() {
        System.out.println("Hello Binder");
    }
}

public class TestBinderCreator implements ApiBinderCreator {
    public TestBinder createBinder(ApiBinder apiBinder) {
        return new TestBinderImpl(apiBinder);
    }
}
```

最后启动 Hasor 并加载配置文件来使用这个扩展：

```java
Hasor.create().mainSettingWith("my-hconfig.xml").build(apiBinder -> {
    TestBinder myBinder = apiBinder.tryCast(TestBinder.class);
    myBinder.hello();
});
```

当程序运行到 `myBinder.hello()` 之后，控制台就会打印出 `"Hello Binder"`。

## 关于tryCast

`tryCast` 会做尝试转换，如果 `apiBinder` 并未加载 `TestBinder` 扩展，`tryCast` 会返回 null。因此 `tryCast` 函数的功效等同于：

```java
if (apiBinder instanceof TestBinder) {
    return (TestBinder)apiBinder;
} else {
    return null;
}
```
