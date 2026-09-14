---
id: injectmembersioc
sidebar_position: 5
title: 2.1.4 InjectMembers Mode
description: Delegate the full injection process to InjectMembers.
---

# 2.1.4 InjectMembers Mode

:::tip
Once a bean implements `net.hasor.core.spi.InjectMembers`, all other injection methods become invalid.
:::

The complete injection process is delegated to the `InjectMembers` interface.

```java title='Example'
public class OrderManager implements InjectMembers {
    @Inject  // <- Because InjectMembers is implemented, the @Inject annotation is ignored.
    public StockManager stockBeanTest;
    public StockManager stockBean;

    public void doInject(AppContext appContext) throws Throwable {
        assert this.stockBeanTest == null;
    }
}
```
