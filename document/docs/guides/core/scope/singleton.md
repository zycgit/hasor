---
id: singleton
sidebar_position: 2
title: 2.3.1 单例模式(Singleton)
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.3.1 单例模式(Singleton)

声明 Bean 的单例一般通过下面这种注解方式：

```java
@Singleton()
public class AopBean {
    ...
}
```

如果您使用的 `ApiBinder` 方式进行代码形式声明单例，那么需要这样：

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(PojoInfo.class).asEagerSingleton();
    }
}
```

## 改为默认单例模式

:::tip
Hasor 不是默认单例的，默认单例可以借助 SPI 实现这个功能。首先创建SPI监听器：
:::

```java
public class MyCollectScopeChainSpi implements CollectScopeChainSpi {
    public Supplier<Scope>[] collectScope(BindInfo<?> bindInfo, AppContext appContext,
										  Supplier<Scope>[] suppliers) {
        // 注册的 Bean 无论是否已经单例，都追加一个单例。
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }

    public Supplier<Scope>[] collectScope(Class<?> targetType, AppContext appContext,
										  Supplier<Scope>[] suppliers) {
        // 非注册的 Bean 无论是否已经单例，都追加一个单例。
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }
}
```

然后创建容器并且设置 SPI：

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
    // 设置默认单例SPI
    apiBinder.bindSpiListener(CollectScopeChainSpi.class, new MyCollectScopeChainSpi());
});
```

最后测试两次创建的 Bean 就是一样的了：

```java
PojoBean pojoBean1 = appContext.getInstance(PojoBean.class);
PojoBean pojoBean2 = appContext.getInstance(PojoBean.class);
assert pojoBean1 == pojoBean2;
```
