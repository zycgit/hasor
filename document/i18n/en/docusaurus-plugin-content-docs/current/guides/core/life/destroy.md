---
id: destroybean
sidebar_position: 4
title: Destroying Beans
description: Configure destroy methods for singleton beans.
---

# Destroying Beans

:::tip
Only singleton objects support destruction.
:::

## Method One

Use the `net.hasor.core.Destroy` or `javax.annotation.PreDestroy` annotation.

Use `@Singleton` together with `@Destroy`. After the bean is created, Hasor tracks it automatically. When the Hasor container enters the destroy process, Hasor calls the destroy method automatically:

```java
@Singleton
public class PojoBean {
    @Destroy
    public void destroy(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class);
    }
}
```

Method two:

The corresponding `ApiBinder` method is `destroyMethod`. Specify it in code during module initialization, as shown below:

```java
public class PojoBean {
    public void destroy(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class)
                .destroyMethod("destroy") // Destroy method, equivalent to @Destroy.
                .asEagerSingleton();      // Singleton, equivalent to @Singleton.
    }
}
```
