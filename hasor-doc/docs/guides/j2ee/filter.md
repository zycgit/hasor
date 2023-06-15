---
id: filter
sidebar_position: 2
title: b.Filter
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# Filter

使用 Filter 如下所示：

```java title='例子'
public class MyFilter implements Filter {
    ...
}
```

然后注册 Filter：

```java title='配置拦截器'
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.jeeFilter("/*").through(MyFilter.class);
        ...
    }
}
```
