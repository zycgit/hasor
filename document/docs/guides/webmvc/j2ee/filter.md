---
id: filter
sidebar_position: 2
title: 4.8.2 Filter
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.8.2 Filter

使用 Filter 如下所示：

```java title='例子'
public class MyFilter implements Filter {
    ...
}
```

然后注册 Filter：

```java title='配置拦截器'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.jeeFilter("/*").through(MyFilter.class);
        ...
    }
}
```
