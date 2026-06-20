---
id: prototype
sidebar_position: 3
title: b. Prototype Mode
description: Use prototype bean scope in Hasor.
---

# Prototype Mode

Prototype mode and singleton mode are opposites. Hasor uses prototype mode by default, so developers do not need any extra configuration.

```java
@Prototype()
public class AopBean {
    ...
}
```

You can also declare it in code through `ApiBinder`:

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(PojoInfo.class).asEagerPrototype();
    }
}
```
