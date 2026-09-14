---
id: interfacseioc
sidebar_position: 4
title: 2.1.3 Interface Injection
description: Declare implementation types for injected interfaces.
---

# 2.1.3 Interface Injection

If the type to inject is an interface, configure the `net.hasor.core.ImplBy` annotation on that interface to determine its concrete implementation class.

```java title='Example'
@ImplBy(PayServiceImpl.class)
public interface PayService {
    ...
}
```

You can also declare the relationship between an interface and its implementation in code during module initialization.

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
    apiBinder.bindType(PayService.class).to(PayServiceImpl.class);
});
```

:::tip
The `@ImplBy` annotation is transitive. The target type of an `ImplBy` annotation can itself be annotated with `@ImplBy` again.
`@ImplBy` can be placed on interfaces, abstract classes, and concrete classes.
:::
