---
id: uniquename
sidebar_position: 3
title: b. Same Type with Different Names
description: Bind multiple named beans of the same type.
---

# Same Type with Different Names

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
