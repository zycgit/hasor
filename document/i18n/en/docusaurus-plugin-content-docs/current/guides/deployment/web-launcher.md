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
import net.hasor.boot.web.WebServers;

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
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>5.0.2-SNAPSHOT</version>
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

When `server` is empty, Hasor uses the first provider discovered through Java SPI. Set the name explicitly when multiple implementations are present. Matching is case-insensitive; a missing provider or unknown name throws an exception. Official names are:

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
HASOR_HTTP_PORT=9090 java -jar demo-hasor-boot-web-5.0.2-SNAPSHOT.jar
```

## Create WebServer Programmatically

If you do not want to read HTTP parameters from a configuration file, build `WebServerConfig` explicitly:

```java
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;

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

`hasor-web/web-hconfig.xml` supplies HTTP defaults. `WebServerConfig.loadSettings` reads only `server`, `host`, `port`, and `contextPath`. Use `filterName`, `filterPattern`, `hconfigFile`, and `documentRoot(File)` for the other options. A new config uses its own defaults; call `loadSettings(Hasor.create().buildSettings())` to read application settings. Port `0` requests an available port; read it with `server.getPort()` after startup.

## Custom AppContext Factory

All three containers support `appContextFactory`, which receives the actual ServletContext and returns the AppContext used for requests:

```java
package com.example;

import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;
import net.hasor.config.ApplicationBoot;
import net.hasor.config.Configuration;

@Configuration
public class Application {
    public static void main(String[] args) throws Exception {
        WebServerConfig config = new WebServerConfig()
                .server("tomcat").port(8080)
                .appContextFactory(sc -> ApplicationBoot.create(sc, Application.class)
                        .bindArguments(args)
                        .registerShutdownHook(false)
                        .build());
        WebServers.run(config).join();
    }
}
```

Add `hasor-config` and a container dependency. See [Java Configuration](../core/conf/java-config.md) for scanning rules. The factory replaces default root-module, config-file, and argument initialization: configure those inside the factory. The default path passes `String[]` in the `hasor-main-args` ServletContext attribute and binds it as `Arguments`; this factory binds arguments directly.

`RuntimeListener` closes the factory-created context when the Web container is destroyed. Retrieve it with `RuntimeListener.getAppContext(servletContext)`. Tomcat checks Web Context availability after startup and cleans up and throws if initialization failed.

## Lifecycle Ownership

`WebServers.create(config)` requires the caller to start and stop the server. `run(config)` starts it and registers a JVM shutdown hook; `join()` waits for it to stop.

Container dependencies also register Modules that start the server during an ordinary AppContext's `onStart` and stop it during `onStop`. These Modules skip initialization when Hasor already has a ServletContext. In automatic startup, the listener creates a separate Web AppContext; do not assume it is the outer context. Use the factory for explicit Web context configuration. Include one container or set `hasor.http.server` explicitly.
