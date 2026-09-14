---
id: singleton
sidebar_position: 2
title: 2.3.1 Singleton Mode
description: Configure singleton beans in Hasor.
---

# 2.3.1 Singleton Mode

A bean singleton is usually declared with the following annotation:

```java
@Singleton()
public class AopBean {
    ...
}
```

If you use `ApiBinder` to declare a singleton in code, use the following form:

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(PojoInfo.class).asEagerSingleton();
    }
}
```

## Changing the Default to Singleton Mode

:::tip
Hasor does not use singleton mode by default. You can use SPI to make singleton the default. First, create an SPI listener:
:::

```java
public class MyCollectScopeChainSpi implements CollectScopeChainSpi {
    public Supplier<Scope>[] collectScope(BindInfo<?> bindInfo, AppContext appContext,
                                          Supplier<Scope>[] suppliers) {
        // Add a singleton scope for every registered bean, whether or not it is already singleton.
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }

    public Supplier<Scope>[] collectScope(Class<?> targetType, AppContext appContext,
                                          Supplier<Scope>[] suppliers) {
        // Add a singleton scope for every unregistered bean, whether or not it is already singleton.
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }
}
```

Then create the container and set up the SPI:

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
    // Set the default singleton SPI.
    apiBinder.bindSpiListener(CollectScopeChainSpi.class, new MyCollectScopeChainSpi());
});
```

Finally, test that two created beans are the same instance:

```java
PojoBean pojoBean1 = appContext.getInstance(PojoBean.class);
PojoBean pojoBean2 = appContext.getInstance(PojoBean.class);
assert pojoBean1 == pojoBean2;
```
