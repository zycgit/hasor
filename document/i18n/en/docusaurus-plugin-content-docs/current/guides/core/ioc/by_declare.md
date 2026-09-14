---
id: declareioc
sidebar_position: 6
title: 2.1.5 Declarative Injection
description: Declare dependency injection relationships in code or configuration.
---

# 2.1.5 Declarative Injection

Using code to describe a class's injection dependencies is called declarative injection. XML and other configuration files can also replace code for this purpose, but the essence is the same.

```java title='Example'
AppContext appContext = Hasor.create().build(apiBinder -> {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // The payService field of TradeService requires dependency injection, and the injected type is PayService.
        apiBinder.bindType(TradeService.class).inject("payService", PayService.class);
        // Because PayService is an interface, specify PayServiceImpl2 as its implementation.
        apiBinder.bindType(PayService.class).to(PayServiceImpl2.class);
    }
});

TradeService myBean = appContext.getInstance(TradeService.class);
```
