---
id: useioc
sidebar_position: 2
title: 4.2.1 Using IoC
description: Use dependency injection in request handlers.
---

# 4.2.1 Using IoC

Fields of request-handler classes can be injected through dependency injection:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Inject
    private PayService payService

    @Any
    public void execute() {
        ...
    }
}
```

:::tip
For dependency injection details, see the Dependency Injection (IoC) section.
:::
