---
id: interfacseioc
sidebar_position: 4
title: c. 接口注入
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 接口注入

如果要注入的对象类型是一个接口，那么需要在这个接口上设置设置 `net.hasor.core.ImplBy` 注解。已确定其具体实现类是谁。

```java title='例如'
@ImplBy(PayServiceImpl.class)
public interface PayService {
    ...
}
```

也可以使用代码方式在 Module 初始化过程中声明接口和实现类的关系。

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
    apiBinder.bindType(PayService.class).to(PayServiceImpl.class);
});
```

:::tip
`@ImplBy` 注解具有传导性。ImplBy 的那个目标类型也可以再次被 @ImplBy
`@ImplBy` 可以标记在：接口、抽象类、实体类 上。
:::
