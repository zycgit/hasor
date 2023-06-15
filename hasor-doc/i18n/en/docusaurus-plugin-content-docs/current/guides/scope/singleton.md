---
id: singleton
sidebar_position: 2
title: a.单例模式(Singleton)
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 单例模式(Singleton)

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
public class MyCollectScopeListener implements CollectScopeListener {
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
    apiBinder.bindSpiListener(CollectScopeListener.class, new MyCollectScopeListener());
});
```

最后测试两次创建的 Bean 就是一样的了：

```java
PojoBean pojoBean1 = appContext.getInstance(PojoBean.class);
PojoBean pojoBean2 = appContext.getInstance(PojoBean.class);
assert pojoBean1 == pojoBean2;
```