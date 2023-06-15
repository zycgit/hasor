---
id: uniquename
sidebar_position: 3
title: b.同类型不同名
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
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
