---
id: web
sidebar_position: 1
title: Web Development
description: Configure and use Hasor Web for Web MVC development.
---

# Web Development

## Project Configuration

Hasor's web support is an independent framework. Before using it, add it to your project. Add the following dependency, and then configure `web.xml`.

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>4.2.2</version><!-- Check the latest version: https://mvnrepository.com/artifact/net.hasor/hasor-web -->
</dependency>
```

Next, configure the `web.xml` file:

```xml
<!-- Framework startup. -->
<listener>
    <listener-class>net.hasor.web.startup.RuntimeListener</listener-class>
</listener>

<!-- Global interceptor. -->
<filter>
    <filter-name>rootFilter</filter-name>
    <filter-class>net.hasor.web.startup.RuntimeFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>rootFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<!-- Recommended startup module. -->
<context-param>
    <param-name>hasor-root-module</param-name>
    <param-value>com.xxx.you.project.StartModule</param-value>
</context-param>

<!-- Optional: specify the configuration file here. -->
<context-param>
    <param-name>hasor-hconfig-file</param-name>
    <param-value>classpath:hasor-config.xml</param-value>
</context-param>
```

Finally, create the package `com.xxx.you.project`, add a `StartModule` class to it, and use the following content:

```java
package com.xxx.you.project;
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        System.out.println("You Project Start.");
    }
}
```

Start your web project. If the console prints `You Project Start.`, the framework has been configured successfully.

The `hasor-root-module` item can be configured equivalently in a configuration file. The advantage of using a configuration file is that it can provide richer configuration. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <!-- Project package: reduce class scanning range. -->
        <loadPackages>com.xxx.you.project.*</loadPackages>
        <!-- Framework startup entry. -->
        <startup>com.xxx.you.project.StartModule</startup>
    </hasor>
</config>
```

## HelloWord

This example shows how to receive a web request with Hasor MVC and display it with JSP. First, create a request handler. A request handler can be as simple as one `execute` method.

```java
@MappingTo("/hello.jsp")
public class HelloMessage {
    public void execute(Invoker invoker) {
        invoker.put("message", "this message form Project.");
    }
}
```

Then register the controller in the startup module.

```java
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        // Set request and response encoding.
        apiBinder.setEncodingCharacter("utf-8", "utf-8");
        // Scan all classes annotated with @MappingTo.
        Set<Class<?>> aClass = apiBinder.findClass(MappingTo.class, "com.example.web.action.*");
        // Configure controllers.
        apiBinder.loadMappingTo(aClass);
    }
}
```

Finally, create `hello.jsp` and print `message`:

```html
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Hello Word</title>
    </head>
    <body>
        ${message}
    </body>
</html>
```

After everything above is ready, start your web project and visit `http://localhost:8080/hello.jsp` to see the result.
