---
id: propsioc
sidebar_position: 3
title: 属性注入
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
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
