---
id: uniquename
sidebar_position: 3
title: 同类型不同名
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 同类型不同名

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(ICache.class).nameWith("user").to(...);
        apiBinder.bindType(ICache.class).nameWith("data").to(...);
    }
}

public class UseBean {
    @Inject("user")
    private ICache user;
    @Inject("data")
    private ICache data;
}
```
