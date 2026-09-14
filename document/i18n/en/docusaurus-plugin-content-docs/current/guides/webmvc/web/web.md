---
id: web
sidebar_position: 1
title: 4.1 Web Development
description: Build Servlet Web MVC applications with hasor-web.
---

# 4.1 Web Development

`hasor-web` provides Web MVC capabilities on top of `hasor-core`. It can run in a traditional Servlet container or start an embedded container through `hasor-boot-web-tomcat`, `hasor-boot-web-jetty`, or `hasor-boot-web-undertow`.

## Add the dependency

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

## Start with web.xml

Traditional Servlet applications use `RuntimeListener` to create the Hasor `AppContext` and `RuntimeFilter` to dispatch requests.

```xml
<listener>
    <listener-class>net.hasor.web.startup.RuntimeListener</listener-class>
</listener>

<filter>
    <filter-name>hasorFilter</filter-name>
    <filter-class>net.hasor.web.startup.RuntimeFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>hasorFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<context-param>
    <param-name>hasor-root-module</param-name>
    <param-value>com.example.web.StartModule</param-value>
</context-param>

<context-param>
    <param-name>hasor-hconfig-file</param-name>
    <param-value>classpath:hconfig.xml</param-value>
</context-param>
```

`hasor-root-module` points to the application startup module. `hasor-hconfig-file` is optional and specifies the Hasor configuration file.

## Write a WebModule

Web applications usually implement `WebModule`, which exposes `WebApiBinder` instead of the plain `ApiBinder`.

```java
package com.example.web;

import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

public class StartModule implements WebModule {
    @Override
    public void loadModule(WebApiBinder apiBinder) {
        apiBinder.setEncodingCharacter("utf-8", "utf-8");
        apiBinder.loadMappingTo(HelloAction.class);
    }
}
```

## Handle a request

Use `@MappingTo` for the path and `@Get`, `@Post`, and similar annotations for HTTP methods.

```java
package com.example.web;

import java.io.IOException;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo("/hello")
public class HelloAction {
    @Get
    public void execute(Invoker invoker) throws IOException {
        invoker.getHttpResponse().setContentType("text/plain;charset=UTF-8");
        invoker.getHttpResponse().getWriter().write("hello Hasor Web");
    }
}
```

Visit:

```text
http://localhost:8080/hello
```

## Start an embedded container

With Hasor Boot, start an embedded container through `WebServers`:

```java
import net.hasor.boot.web.WebServers;

public class DemoWebApplication implements WebModule {
    public static void main(String[] args) throws Exception {
        WebServers.run(args, DemoWebApplication.class).join();
    }

    @Override
    public void loadModule(WebApiBinder apiBinder) {
        apiBinder.loadMappingTo(HelloAction.class);
    }
}
```

Embedded container settings come from `hasor.boot.web` and can also be overridden by environment variables:

```xml
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
    </hasor.boot.web>
</config>
```

The container is discovered through SPI and must be unique; there is no name selector.
