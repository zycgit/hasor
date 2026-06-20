---
id: constructorioc
sidebar_position: 2
title: a. Constructor Injection
description: Use constructors to inject dependencies in Hasor.
---

# Constructor Injection

When a class has exactly one no-argument constructor, you do not need to use `@ConstructorBy` to indicate which constructor should be used to build the bean.

First, annotate the constructor to be injected with `net.hasor.core.ConstructorBy`. This tells Hasor to use that constructor when creating the bean.

Then, if parameters need to be injected, add the `net.hasor.core.Inject` annotation before the parameters. This indicates that the parameter value comes from dependency injection.

```java title='Example'
public class CustomBean {
    private FunBean funBean = null;

    @ConstructorBy
    public CustomBean(@Inject() FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();
    }
}
```

Creating the bean is also simple. There is no need to register or declare the bean. Just obtain it from the container by type, and Hasor automatically resolves the configuration while creating the bean.

```java
AppContext appContext = Hasor.create().build();
CustomBean myBean = appContext.getInstance(CustomBean.class);
```

:::tip
The `net.hasor.core.ConstructorBy` annotation has the same effect as `javax.inject.Inject`.
If multiple constructors are annotated with `ConstructorBy`, they are sorted by parameter count, and the constructor with the fewest parameters is used as the final constructor.
:::
