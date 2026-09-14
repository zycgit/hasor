---
id: injectmembersioc
sidebar_position: 5
title: 2.1.4 InjectMembers方式
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.1.4 InjectMembers方式

:::tip
Bean 一旦实现 `net.hasor.core.spi.InjectMembers` 接口，那么其它所有注入方式全部失效
:::

具体的注入的全部过程会被委托给 InjectMembers 接口处理。

```java title='例如'
public class OrderManager implements InjectMembers {
    @Inject  // <-因为实现了InjectMembers接口，因此@Inject注解将会失效。
    public StockManager stockBeanTest;
    public StockManager stockBean;

    public void doInject(AppContext appContext) throws Throwable {
        assert this.stockBeanTest == null;
    }
}
```
