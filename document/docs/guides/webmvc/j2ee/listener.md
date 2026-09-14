---
id: listener
sidebar_position: 3
title: 4.8.3 Listener
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.8.3 Listener

J2EE 规范中定义了各种各样的 Listener 例如 `javax.servlet.http.HttpSessionListener`

这些 Listener 基本在 Hasor 中都是都是支持的，配置它们需要通过 SPI 的形式来注册。例如：

```java title='例子'
public class MyHttpSessionListener implements HttpSessionListener {
    ...
}

public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.bindSpiListener(HttpSessionListener.class, new MyHttpSessionListener());
        ...
    }
}
```

目前 Hasor 已经支持的 J2EE Listener清单有：
- `javax.servlet.http.HttpSessionListener`
- `javax.servlet.ServletContextListener`
- `javax.servlet.ServletRequestListener`
