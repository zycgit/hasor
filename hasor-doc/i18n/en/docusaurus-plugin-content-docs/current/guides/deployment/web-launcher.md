---
id: web-launcher
sidebar_position: 4
title: Web Launcher
description: Use WebServers.run to start Hasor Web applications and embedded Servlet containers.
---

# Web Launcher

Web applications use `WebServers.run(args, RootModule.class)` to start an embedded Servlet container. It creates Tomcat, Jetty, or Undertow from the available `WebServerProvider` on the classpath, and automatically registers the Hasor Web `RuntimeListener` and `RuntimeFilter`.

```java
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.http.WebServers;

public class DemoHasorBootWebApplication implements WebModule {
    public static void main(String[] args) throws Exception {
        WebServers.run(args, DemoHasorBootWebApplication.class).join();
    }

    @Override
    public void loadModule(WebApiBinder apiBinder) {
        apiBinder.setEncodingCharacter("utf-8", "utf-8");
        apiBinder.loadMappingTo(HelloWebAction.class);
    }
}
```

Web startup needs at least one embedded container module:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-tomcat</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

## Configure Startup Parameters

`WebServers.run(args, RootModule.class)` reads `hconfig.xml` first, then loads embedded-container parameters from the `hasor.http` configuration.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <http>
            <server>tomcat</server>
            <host>0.0.0.0</host>
            <port>8080</port>
            <contextPath>/</contextPath>
        </http>
    </hasor>
</config>
```

When `server` is empty, Hasor discovers an available container through Java SPI. The official container names are:

- `tomcat`
- `jetty`
- `undertow`

The default `hasor-web` configuration already supports environment variable overrides:

```xml
<server>${HASOR_HTTP_SERVER}</server>
<host>${HASOR_HTTP_HOST:0.0.0.0}</host>
<port>${HASOR_HTTP_PORT:8080}</port>
<contextPath>${HASOR_HTTP_CONTEXT_PATH:/}</contextPath>
```

You can therefore change the runtime port through an environment variable:

```bash
HASOR_HTTP_PORT=9090 java -jar demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```

## Create WebServer Programmatically

If you do not want to read HTTP parameters from a configuration file, build `WebServerConfig` explicitly:

```java
import net.hasor.web.http.WebServer;
import net.hasor.web.http.WebServerConfig;
import net.hasor.web.http.WebServers;

public class DemoHasorBootWebApplication implements WebModule {
    public static void main(String[] args) throws Exception {
        WebServerConfig config = WebServerConfig
                .of(DemoHasorBootWebApplication.class)
                .server("tomcat")
                .host("0.0.0.0")
                .port(8080)
                .contextPath("/")
                .arguments(args);

        WebServer server = WebServers.run(config);
        server.join();
    }
}
```

`hasor-web` already provides a default `hasor.http` section in `web-hconfig.xml`. Applications only need to override a few runtime parameters in their own `hconfig.xml`. The Hasor Web `RuntimeFilter` name, match path, boot entry configuration file, and ServletContext static resource root are fixed internally by the framework and do not need to be exposed as HTTP configuration items.
