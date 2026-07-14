---
id: web-launcher
sidebar_position: 4
title: Web 启动器
description: 使用 WebServers.run 启动 Hasor Web 应用和内嵌 Servlet 容器。
---

# Web 启动器

Web 应用使用 `WebServers.run(args, RootModule.class)` 启动内嵌 Servlet 容器。它会根据 classpath 中可用的 `WebServerProvider` 创建 Tomcat、Jetty 或 Undertow，并自动注册 Hasor Web 的 `RuntimeListener` 和 `RuntimeFilter`。

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

Web 启动至少需要加入一个内嵌容器模块：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-tomcat</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

## 配置启动参数

`WebServers.run(args, RootModule.class)` 会先读取 `hconfig.xml`，然后从 `hasor.http` 配置中获取内嵌容器参数。

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

`server` 为空时会通过 Java SPI 自动发现可用容器。官方容器名称为：

- `tomcat`
- `jetty`
- `undertow`

`hasor-web` 的默认配置已经支持环境变量覆盖：

```xml
<server>${HASOR_HTTP_SERVER}</server>
<host>${HASOR_HTTP_HOST:0.0.0.0}</host>
<port>${HASOR_HTTP_PORT:8080}</port>
<contextPath>${HASOR_HTTP_CONTEXT_PATH:/}</contextPath>
```

因此也可以通过环境变量调整运行端口：

```bash
HASOR_HTTP_PORT=9090 java -jar demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```

## 编码方式创建 WebServer

如果不希望从配置文件读取 HTTP 参数，可以显式构建 `WebServerConfig`：

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

`hasor-web` 的 `web-hconfig.xml` 中已经提供了默认的 `hasor.http` 小节，应用只需要在自己的 `hconfig.xml` 中覆盖少数运行参数。Hasor Web 的 `RuntimeFilter` 名称、匹配路径、boot 入口配置文件以及 ServletContext 静态资源根目录由框架内部固定处理，不需要暴露为 HTTP 配置项。
