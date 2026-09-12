---
id: j2eefilter
sidebar_position: 2
title: Filter形式
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# Filter形式

传统的 J2EE的 Filter 充当拦截器。例如：

```java title='例子'
public class MyFilter implements Filter {
    ...
}
```

最后对拦截器进行声明注册就可以正常使用了。

```java title='配置拦截器'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.jeeFilter("/*").through(MyFilter.class);     // Filter形式
        ...
    }
}
```
