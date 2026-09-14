---
id: proxybean
sidebar_position: 4
title: 2.2.3 Delegated Bean Creation
description: Delegate Hasor bean creation to another IoC container.
---

# 2.2.3 Delegated Bean Creation

Delegated bean creation was added after version 4.2.1. With `TypeSupplier`, beans registered in Hasor can be delegated to another IoC container for creation.

Before this, Hasor bean creation only supported the factory approach, as shown below:

```java
public class TypeBean1Factory implements Supplier<TypeBean1> {
    private TypeBean1 target = new TypeBean1();

    public TypeBean1 get() {
        return target; // Create TypeBean1 through the factory.
    }
}

public class TypeBean2Factory implements Supplier<TypeBean2> {
    private TypeBean2 target = new TypeBean2();

    public TypeBean2 get() {
        return target; // Create TypeBean2 through the factory.
    }
}

AppContext appContext = Hasor.create().build(apiBinder -> {
    // Create factories.
    TypeBean1Factory factory1 = new TypeBean1Factory();
    TypeBean2Factory factory2 = new TypeBean2Factory();
    // Register beans and specify their factories.
    apiBinder.bindType(TypeBean1.class).toProvider(factory1);
    apiBinder.bindType(TypeBean2.class).toProvider(factory2);
});

// Create beans through factories.
TypeBean1 typeBean1 = appContext.getInstance(TypeBean1.class);
TypeBean2 typeBean2 = appContext.getInstance(TypeBean2.class);
```
