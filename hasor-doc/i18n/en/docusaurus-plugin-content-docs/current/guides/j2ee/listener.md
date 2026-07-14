---
id: listener
sidebar_position: 3
title: c. Listener
description: Register J2EE listeners through Hasor SPI.
---

# Listener

The J2EE specification defines many listeners, such as `javax.servlet.http.HttpSessionListener`.

Most of these listeners are supported in Hasor. Configure them by registering through SPI. For example:

```java title='Example'
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

The J2EE listeners currently supported by Hasor include:
- `javax.servlet.http.HttpSessionListener`
- `javax.servlet.ServletContextListener`
- `javax.servlet.ServletRequestListener`
