---
id: declareioc
sidebar_position: 6
title: e.声明式注入
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 声明式注入

用代码的形式来说明类的注入依赖关系就叫声明式注入，也可以利用 Xml 等配置文件来替代代码。但是本质是一样的。

```java title='例如'
AppContext appContext = Hasor.create().build(apiBinder -> {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // .类型 TradeService 的 payService 字段要求依赖注入，注入的类型是 PayService
        apiBinder.bindType(TradeService.class).inject("payService", PayService.class);
        // .由于 PayService 是一个接口，因此指定 PayService 的实现类为 PayServiceImpl2
        apiBinder.bindType(PayService.class).to(PayServiceImpl2.class);
    }
});

TradeService myBean = appContext.getInstance(TradeService.class);
```
