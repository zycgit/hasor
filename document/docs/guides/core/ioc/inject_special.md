---
id: specialioc
sidebar_position: 8
title: 2.1.8 注入容器类型
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.1.8 注入容器类型

例如：得到容器自身

```java
public class AwareBean {
    @Inject()
    private AppContext appContext
}

//或

public class AwareBean implements AppContextAware {
    public void setAppContext(AppContext appContext) {
        ...
    }
}
```

其它容器可以被注入的特殊类型：

| 接口                              | 功效                |
|---------------------------------|-------------------|
| `net.hasor.core.AppContext`     | 容器自身              |
| `net.hasor.cobble.setting.Settings` | 读取配置接口        |
| `net.hasor.core.spi.SpiTrigger` | SPI 触发器           |
| `net.hasor.core.EventContext`   | 容器事件模型接口          |
| `javax.servlet.ServletContext`  | J2EE 的 Servlet 容器 |
| `net.hasor.web.ServletVersion`  | Servlet 容器版本      |
| `net.hasor.web.MimeType`        | 可以根据扩展名查询对应的 mime |
