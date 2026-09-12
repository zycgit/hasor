---
id: uniqueid
sidebar_position: 2
title: 唯一ID
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 唯一ID

与 Spring一样，可以为 Bean 指定唯一的名称。也就是Bean的 ID。

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(InfoBean.class).idWith("beanA");
        apiBinder.bindType(InfoBean.class).idWith("beanB");
    }
}
```

获得带名称的 Bean 可以通过 `appContext.getInstance("beanA")` 或者在依赖注入中指定名字：

```java
public class UseBean {
    @Inject(value = "beanA" , byType = Type.ByID)
    private InfoBean pojoA;
    @Inject(value = "beanB" , byType = Type.ByID)
    private InfoBean pojoB;
}
```
