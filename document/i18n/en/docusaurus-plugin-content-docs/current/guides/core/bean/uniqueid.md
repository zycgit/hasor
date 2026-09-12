---
id: uniqueid
sidebar_position: 2
title: Unique ID
description: Assign unique IDs to Hasor beans.
---

# Unique ID

As in Spring, you can assign a unique name to a bean. This name is the bean ID.

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(InfoBean.class).idWith("beanA");
        apiBinder.bindType(InfoBean.class).idWith("beanB");
    }
}
```

A named bean can be obtained with `appContext.getInstance("beanA")`, or the name can be specified during dependency injection:

```java
public class UseBean {
    @Inject(value = "beanA" , byType = Type.ByID)
    private InfoBean pojoA;
    @Inject(value = "beanB" , byType = Type.ByID)
    private InfoBean pojoB;
}
```
