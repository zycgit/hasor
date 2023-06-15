---
id: prototype
sidebar_position: 3
title: b.原型模式(Prototype)
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 原型模式(Prototype)

原型模式 和单例模式是正反的一对关系。Hasor 默认使用的是原型模式，因此开发者不需要做任何配置。

```java
@Prototype()
public class AopBean {
    ...
}
```

或者您可以通过 `ApiBinder` 方式进行代码形式声明：

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(PojoInfo.class).asEagerPrototype();
    }
}
```
