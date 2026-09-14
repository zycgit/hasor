---
id: web
sidebar_position: 1
title: 4.1 Web 开发
description: 使用 hasor-web 构建 Servlet Web MVC 应用。
---

# 4.1 Web 开发

`hasor-web` 在 `hasor-core` 之上提供 Web MVC 能力。它可以运行在传统 Servlet 容器中，也可以通过 `hasor-boot-web-tomcat`、`hasor-boot-web-jetty`、`hasor-boot-web-undertow` 启动内嵌容器。

## 引入依赖

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

## web.xml 启动

传统 Servlet 应用通过 `RuntimeListener` 创建 Hasor `AppContext`，通过 `RuntimeFilter` 接管请求分发。

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

`hasor-root-module` 指向应用的启动模块。`hasor-hconfig-file` 是可选项，用于指定 Hasor 配置文件。

## 编写 WebModule

Web 应用通常实现 `WebModule`，它会把普通 `ApiBinder` 扩展为 `WebApiBinder`。

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

## 接收请求

使用 `@MappingTo` 声明请求路径，使用 `@Get`、`@Post` 等注解声明 HTTP 方法。

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

启动后访问：

```text
http://localhost:8080/hello
```

## 内嵌容器启动

如果使用 Hasor Boot，可以直接通过 `WebServers` 启动内嵌容器：

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

内嵌容器参数来自 `hasor.boot.web` 配置，也可以通过环境变量覆盖：

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

容器通过唯一 SPI 实现自动发现，不提供名称选择。
