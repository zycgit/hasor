---
id: web-launcher
sidebar_position: 4
title: 5.3 Web Launcher
description: Start Hasor Web applications and embedded Servlet containers with WebServers.run.
---

# 5.3 Web Launcher

Prefer the [unified Boot entry point](./boot-launcher.md) for new applications. This page covers the `WebServers` API for managing Web containers directly.

Web applications start an embedded Servlet container through `WebServers.run(args, RootModule.class)`. It creates Tomcat, Jetty, or Undertow using the `WebServerProvider` available on the classpath and automatically registers Hasor Web `RuntimeListener` and `RuntimeFilter`.

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

Web startup requires an embedded container module:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

## Configuring startup parameters

`WebServers.run(args, RootModule.class)` first reads `hconfig.xml`, then obtains embedded container parameters from `hasor.boot.web`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor.boot.web>
        <server>
            <contextPath>/</contextPath>
        </server>
        <connectors>
            <http enabled="true">
                <host>0.0.0.0</host>
                <port>8080</port>
            </http>
        </connectors>
        <health enabled="true">
            <path>/health</path>
        </health>
    </hasor.boot.web>
</config>
```

Dependencies determine the container. SPI must discover exactly one implementation; zero or multiple implementations cause errors. Selection by name is not provided.

- `hasor-boot-web-tomcat`
- `hasor-boot-web-jetty`
- `hasor-boot-web-undertow`

The default configuration in `hasor-boot-web` already supports environment variable overrides:

```xml
<host>${HASOR_HTTP_HOST:0.0.0.0}</host>
<port>${HASOR_HTTP_PORT:8080}</port>
<contextPath>${HASOR_WEB_CONTEXT_PATH:/}</contextPath>
```

You can therefore change the listening port through an environment variable:

```bash
HASOR_HTTP_PORT=9090 java -jar demo-hasor-boot-web-@project.docsVersion@.jar
```

## Creating a WebServer in code

To avoid reading HTTP parameters from a configuration file, construct `WebServerConfig` explicitly:

```java
import net.hasor.boot.web.WebServer;
import net.hasor.boot.web.WebServerConfig;
import net.hasor.boot.web.WebServers;

public class DemoHasorBootWebApplication implements WebModule {
    public static void main(String[] args) throws Exception {
        WebServerConfig config = WebServerConfig
                .of(DemoHasorBootWebApplication.class)

                .host("0.0.0.0")
                .port(8080)
                .contextPath("/")
                .arguments(args);

        WebServer server = WebServers.run(config);
        server.join();
    }
}
```

Defaults are provided by `boot-web-hconfig.xml` in `hasor-boot-web`: `server.contextPath` sets the context path; `connectors.http` contains enabled, host, and port; `health` contains enabled and path. Disabling HTTP leaves no listening port but still initializes the Web context; getPort() returns -1. HTTPS is not yet implemented.

`new WebServerConfig()` uses its own defaults without reading `hconfig.xml` automatically. To use file configuration, call `loadSettings(Hasor.create().buildSettings())`, then override specific values with fluent methods. Port `0` requests a system-assigned port; obtain it through `server.getPort()` after startup.

## Custom AppContext creation

Tomcat, Jetty, and Undertow all support `appContextFactory`. The factory receives the container `ServletContext` and returns the `AppContext` used for request processing, allowing integration with `hasor-config`:

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

                .port(8080)
                .appContextFactory(sc -> ApplicationBoot.create(sc, Application.class)
                        .bindArguments(args)
                        .registerShutdownHook(false)
                        .build());
        WebServers.run(config).join();
    }
}
```

This example needs both `hasor-config` and a container module. `ApplicationBoot` does not infer the startup class package. Set `hasor.loadPackages` to discover configuration classes and Web Controllers; see [Java Annotation Configuration](../core/conf/java-config.md).

When a factory is configured, `RuntimeListener` uses its result and skips default `rootModule`, `hconfigFile`, and startup argument binding. The factory must configure those behaviors itself. The default flow passes `String[]` through ServletContext attribute `hasor-main-args`, then binds it as `Arguments`; the factory example binds `args` directly.

`RuntimeListener` closes the factory-created context when the Web container is destroyed. Obtain it through `RuntimeListener.getAppContext(servletContext)`. Tomcat also checks Web Context availability after startup; initialization failures throw an exception and clean up the container.

## Startup and shutdown ownership

`WebServers.create(config)` only creates the service object; the caller owns `start()` and `stop()`. `WebServers.run(config)` starts it and registers a JVM shutdown hook; `join()` waits for it to stop.

Container dependencies also register an auto-start Module: `onStart` of an ordinary `Hasor.create().build()` starts the service, and closing the outer AppContext stops it. These Modules skip startup when Hasor is already bound to a `ServletContext`, avoiding a second container. In auto-start mode, `RuntimeListener` creates the Web AppContext; do not assume it is the same object as the outer AppContext. Use the factory above when you need to configure the request context explicitly. Auto-start mode also requires exactly one container.
