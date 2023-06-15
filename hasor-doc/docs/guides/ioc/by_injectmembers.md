---
id: injectmembersioc
sidebar_position: 5
title: d.InjectMembers方式
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# InjectMembers方式

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
