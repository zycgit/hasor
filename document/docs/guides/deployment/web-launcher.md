---
id: web-launcher
sidebar_position: 4
title: Web 启动器
description: 使用 WebServers.run 启动 Hasor Web 应用和内嵌 Servlet 容器。
---

# Web 启动器

新应用优先使用[统一 Boot 入口](./boot-launcher.md)。本页介绍直接管理 Web 容器的 `WebServers` API。

Web 应用使用 `WebServers.run(args, RootModule.class)` 启动内嵌 Servlet 容器。它会根据 classpath 中可用的 `WebServerProvider` 创建 Tomcat、Jetty 或 Undertow，并自动注册 Hasor Web 的 `RuntimeListener` 和 `RuntimeFilter`。

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

Web 启动至少需要加入一个内嵌容器模块：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>5.1.1-SNAPSHOT</version>
</dependency>
```

## 配置启动参数

`WebServers.run(args, RootModule.class)` 会先读取 `hconfig.xml`，然后从 `hasor.boot.web` 配置中获取内嵌容器参数。

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

容器由依赖决定，SPI 必须发现唯一实现；零个或多个实现均报错，不提供名称选择。

- `hasor-boot-web-tomcat`
- `hasor-boot-web-jetty`
- `hasor-boot-web-undertow`

`hasor-boot-web` 的默认配置已经支持环境变量覆盖：

```xml
<host>${HASOR_HTTP_HOST:0.0.0.0}</host>
<port>${HASOR_HTTP_PORT:8080}</port>
<contextPath>${HASOR_WEB_CONTEXT_PATH:/}</contextPath>
```

因此也可以通过环境变量调整运行端口：

```bash
HASOR_HTTP_PORT=9090 java -jar demo-hasor-boot-web-5.1.1-SNAPSHOT.jar
```

## 编码方式创建 WebServer

如果不希望从配置文件读取 HTTP 参数，可以显式构建 `WebServerConfig`：

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

默认配置由 `hasor-boot-web` 的 `boot-web-hconfig.xml` 提供：`server.contextPath` 设置上下文路径；`connectors.http` 包含 enabled、host、port；`health` 包含 enabled、path。关闭 HTTP 不监听端口，但仍初始化 Web 上下文，getPort() 返回 -1。HTTPS 尚未实现。

`new WebServerConfig()` 使用自身默认值，不会自动读取 `hconfig.xml`。需要结合配置文件时可调用 `loadSettings(Hasor.create().buildSettings())`，然后用链式方法覆盖具体值。端口 `0` 表示由系统分配，启动后通过 `server.getPort()` 获取实际端口。

## 自定义 AppContext 创建

Tomcat、Jetty、Undertow 均支持 `appContextFactory`。工厂接收容器的 `ServletContext`，返回供请求处理使用的 `AppContext`，可用于接入 `hasor-config`：

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

此例需要同时引入 `hasor-config` 和容器模块。`ApplicationBoot` 不会自动推导启动类所在包；必须用 `hasor.loadPackages` 指定扫描范围，才能发现配置类和 Web Controller，具体规则见 [Java 注解配置](../core/conf/java-config.md)。

配置工厂后，`RuntimeListener` 使用工厂结果，跳过默认的 `rootModule`、`hconfigFile` 和启动参数绑定流程；这些行为需要工厂自行设置。默认流程会通过 ServletContext 属性 `hasor-main-args` 传递 `String[]`，再绑定为 `Arguments`；工厂示例则直接绑定 `args`。

工厂创建的上下文由 `RuntimeListener` 在 Web 容器销毁时关闭，可通过 `RuntimeListener.getAppContext(servletContext)` 获取。Tomcat 启动后还会检查 Web Context 是否可用，初始化失败时抛出异常并清理容器。

## 启动与关闭的归属

`WebServers.create(config)` 只创建服务对象，需要调用方负责 `start()`、`stop()`；`WebServers.run(config)` 会启动服务并注册 JVM shutdown hook，`join()` 用于等待服务停止。

容器依赖还会注册自动启动 Module：普通 `Hasor.create().build()` 的 `onStart` 会启动服务，外层 AppContext 关闭时停止服务。当 Hasor 已绑定 `ServletContext` 时，这些 Module 会跳过，避免再次启动容器。自动启动模式会由 `RuntimeListener` 创建 Web AppContext，不能假定它与外层 AppContext 是同一个对象；需要显式配置请求使用的上下文时使用上面的工厂方式。自动启动模式下也应只引入一个容器。
