---
id: startbean
sidebar_position: 3
title: Creating Beans at Startup
description: Create singleton beans eagerly during Hasor startup.
---

# Creating Beans at Startup

## Method One

If you use `@Singleton` and `@Init` together, and the class is pre-registered through a module during Hasor startup, Hasor automatically creates the class and calls its `init` method. For example:

```java
@Singleton
public class PojoBean {
    @Init
    public void init(){
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

Specify it in code during module initialization, as shown below:

```java
public class PojoBean {
    public void init(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class)
                .initMethod("init")    // Initialization method, equivalent to @Init.
                .asEagerSingleton();   // Singleton, equivalent to @Singleton.
    }
}
```
