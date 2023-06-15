---
id: propsioc
sidebar_position: 3
title: b. 属性注入
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 属性注入

属性注入只需要在需要注入的字段前面加上 net.hasor.core.Inject 注解就可以了

```java title='例如'
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
