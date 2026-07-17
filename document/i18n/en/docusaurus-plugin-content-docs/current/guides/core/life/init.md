---
id: initbean
sidebar_position: 2
title: a. Initializing Beans
description: Configure initialization methods for Hasor beans.
---

# Initializing Beans

Sometimes you want a bean to automatically call an `init` method when it is created. This section shows Hasor's support for that capability.

## Method One

Use the `net.hasor.core.Init` or `javax.annotation.PostConstruct` annotation, as shown below:

```java
public class PojoBean {
    @Init
    public void init(){
        ...
    }
}
```

Method two:

```java
public class PojoBean {
    // Do not use an annotation; specify it through apiBinder instead.
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
