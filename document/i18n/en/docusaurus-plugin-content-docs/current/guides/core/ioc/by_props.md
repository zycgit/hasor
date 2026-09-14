---
id: propsioc
sidebar_position: 3
title: 2.1.2 Property Injection
description: Inject dependencies into fields with Hasor.
---

# 2.1.2 Property Injection

For property injection, add the `net.hasor.core.Inject` annotation to the field that needs to be injected.

```java title='Example'
public class TradeService {
    @Inject
    private PayService payService;

    public boolean foo(){
        ...
    }
}

public class PayService {
    ...
}
```
