---
id: specialioc
sidebar_position: 8
title: 2.1.8 Injecting Container Types
description: Inject Hasor container objects and other special framework types.
---

# 2.1.8 Injecting Container Types

For example, obtain the container itself:

```java
public class AwareBean {
    @Inject()
    private AppContext appContext
}

// Or:

public class AwareBean implements AppContextAware {
    public void setAppContext(AppContext appContext) {
        ...
    }
}
```

Other special types that can be injected by the container:

| Interface                         | Purpose                                      |
|-----------------------------------|----------------------------------------------|
| `net.hasor.core.AppContext`       | The container itself                         |
| `net.hasor.cobble.setting.Settings` | Configuration-reading interface            |
| `net.hasor.core.spi.SpiTrigger`   | SPI trigger                                  |
| `net.hasor.core.EventContext`     | Container event model interface             |
| `javax.servlet.ServletContext`    | J2EE Servlet container                       |
| `net.hasor.web.ServletVersion`    | Servlet container version                    |
| `net.hasor.web.MimeType`          | Looks up the corresponding MIME type by file extension |
