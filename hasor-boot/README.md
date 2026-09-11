# Hasor Boot

Hasor Boot 为 Hasor 应用提供统一启动入口、扩展生命周期和可执行 JAR 打包支持。普通应用与引入 Web 扩展的应用使用同一个 `Boot` 入口，不设置 Web 或非 Web 模式。Web 扩展负责内嵌容器，Boot 主体不依赖 Servlet。

## 特性

- **独立运行**：应用及依赖打包为可执行 JAR，也支持直接运行 Java 启动类。
- **统一启动**：通过 `Boot.run(...)` 启动应用，Java SPI 扩展可在容器创建前准备运行环境。
- **容器选择**：支持 Tomcat、Jetty、Undertow，使用相同的启动 API 和公共 HTTP 配置。
- **默认装配**：统一使用 Core 的 `hasor.loadPackages` 作为扫描范围，兼容已有的 `Module`、`WebModule`。
- **前端托管**：默认加载类路径下的静态资源，支持欢迎页及显式配置的单页应用路由回退。
- **构建集成**：提供 Gradle `bootJar` 任务和 Maven `repackage` 目标，保留依赖 JAR，不要求将依赖类合并展开。

Boot 负责启动和装配；路由、参数绑定、响应渲染等请求处理能力由 Hasor Web 提供，通用 Web 默认装配由 Hasor Config 提供。

## 快速开始

以下示例对应当前源码版本 `5.1.1-SNAPSHOT`，使用 Java 17。快照版本需要先发布到本地 Maven 仓库或配置可用的快照仓库，不代表该版本已在公共仓库发布。

### 1. 配置 Gradle 工程

在 `settings.gradle` 中配置插件仓库：

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = 'demo'
```

在 `build.gradle` 中声明容器依赖及打包启动器：

```groovy
plugins {
    id 'java'
    id 'net.hasor.boot' version '5.1.1-SNAPSHOT'
}

group = 'example'
version = '1.0.0'

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    bootLoader
}

dependencies {
    implementation 'net.hasor:hasor-boot-web-tomcat:5.1.1-SNAPSHOT'
    bootLoader 'net.hasor:hasor-boot-loader:5.1.1-SNAPSHOT'
}

tasks.named('bootJar') {
    mainClass.set('example.Application')
    loaderClasspath.from(configurations.bootLoader)
}
```

将容器依赖替换为 `hasor-boot-web-jetty` 或 `hasor-boot-web-undertow` 即可切换容器。只能引入一种容器；SPI 发现零个或多个实现都会报错。

普通应用将容器依赖替换为 `net.hasor:hasor-boot:5.1.1-SNAPSHOT`，不需要 Servlet 或 JSON 库；入口仍使用下面的 `Boot.run(...)`。一次性任务完成后调用返回对象的 `close()`，常驻服务可调用 `join()` 等待关闭。

### 2. 编写启动类和接口

`src/main/java/example/Application.java`：

```java
package example;

import net.hasor.boot.Boot;

public class Application {
    public static void main(String[] args) throws Exception {
        Boot.run(args, Application.class).join();
    }
}
```

`src/main/java/example/HelloAction.java`：

```java
package example;

import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo("/hello")
public class HelloAction {
    @Get
    public String execute() {
        return "Hello Hasor Boot";
    }
}
```

启动类无需实现框架接口。按 `hasor.loadPackages` 扫描 `@MappingTo` 类型，不会根据启动类位置推导扫描范围。请在下述配置文件中明确限定业务包。启动类若实现 `Module` 或 `WebModule`，仍会作为应用模块装载。

### 3. 配置扫描范围并运行

先创建 `src/main/resources/hconfig.xml`：

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.loadPackages>example.*</hasor.loadPackages>
</config>
```


```bash
./gradlew bootJar
java -jar build/libs/demo-1.0.0-boot.jar
```

默认监听 `0.0.0.0:8080`，访问 `http://localhost:8080/hello` 可得到文本响应。没有提供首页时，访问根路径不会自动生成控制台页面。

`bootJar` 默认在普通 JAR 文件名后添加 `-boot`，并接入 `assemble`。入口类从 `bootJar.mainClass` 读取，未设置时读取普通 JAR 的 `Main-Class`；插件不会自动搜索主类，也不会自动配置 `loaderClasspath`。

## 配置应用

### 配置文件

`Boot.run(args, Application.class)` 会先加载 Hasor 设置，包括应用的 `hconfig.xml`。在 `src/main/resources/hconfig.xml` 中配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.loadPackages>example.*</hasor.loadPackages>
    <hasor.boot.web>
        <server>
            <contextPath>/</contextPath>
        </server>
        <connectors>
            <http>
                <enabled>true</enabled>
                <host>0.0.0.0</host>
                <port>8080</port>
            </http>
        </connectors>
        <health>
            <enabled>true</enabled>
            <path>/health</path>
        </health>
    </hasor.boot.web>
    <hasor.config.web>
        <spaPaths>/app/*</spaPaths>
    </hasor.config.web>
</config>
```

公共 HTTP 默认配置由 `hasor-boot-web` 的 `boot-web-hconfig.xml` 提供：

| 配置项 | 默认值 | 环境变量 |
| --- | --- | --- |
| `hasor.boot.web.connectors.http.enabled` | `true` | `HASOR_HTTP_ENABLED` |
| `hasor.boot.web.connectors.http.host` | `0.0.0.0` | `HASOR_HTTP_HOST` |
| `hasor.boot.web.connectors.http.port` | `8080` | `HASOR_HTTP_PORT` |
| `hasor.boot.web.server.contextPath` | `/` | `HASOR_WEB_CONTEXT_PATH` |

环境变量由默认配置中的占位符读取；应用可以在配置文件中覆盖这些默认值。启动参数会传递给应用，不会自动将 `--port=...` 等参数解析为 HTTP 配置。

容器由依赖决定，SPI 必须发现唯一的容器实现，不提供名称选择配置。

关闭 `connectors.http.enabled` 后仍初始化 Web 应用上下文，但不监听 HTTP 端口，`WebServer.getPort()` 返回 `-1`。目前只实现 HTTP 监听，HTTPS 等协议尚未提供。

原 `hasor.http.*` 配置需迁移到上述结构；容器选择配置已删除，上下文路径的环境变量改为 `HASOR_WEB_CONTEXT_PATH`，HTTP 的 host、port 环境变量保持不变。

Config、Web、Boot 不再读取专用 `scanPackages` 或 `autoScan` 配置，也不提供对应的设置接口。自动扫描默认执行，范围仅由 Core 的 `hasor.loadPackages`（环境变量 `HASOR_LOAD_PACKAGES`）管理。

Web 装配选项使用 `hasor.config.web.*`，主要包括 `scanExcludes`、`staticResources`、`staticLocation`、`spaPaths`、`resourceExcludes` 和 `response.noStorePaths`。数组项通过重复 XML 元素配置。

当前实现仍兼容读取 `hasor.boot.web.*`，先读取旧前缀，再读取 `hasor.config.web.*`；后者的有效配置覆盖对应旧值，扫描排除项则累积。新配置统一使用 `hasor.config.web.*`，避免混用。

### 代码配置与生命周期

需要在文件配置基础上覆盖部分值时，先加载设置，再调用配置方法：

```java
WebServerConfig config = WebServerConfig
        .of(Hasor.create().buildSettings(), Application.class)
        .port(9090)
        .spaPaths("/app/*");

WebServers.run(config).join();
```

上述类型分别来自 `net.hasor.boot.web.WebServerConfig`、`net.hasor.core.Hasor` 和 `net.hasor.boot.web.WebServers`。单独调用 `WebServerConfig.of(Application.class)` 不会为这个配置对象读取文件中的 HTTP 与 Web 装配选项。

- `Boot.run(...)`：统一启动入口，返回 `BootApplication`，通过 `getAppContext()` 获取容器，`close()` 释放扩展和容器。
- `new Boot().sources(...).property(...).start()`：代码式配置统一入口，属性优先于文件配置。
- `WebServers.run(...)`：创建并启动容器，注册 JVM 关闭钩子。
- `WebServers.create(...)`：只创建容器，由调用方执行 `start()`、`stop()`。
- `join()`：等待容器停止；`port(0)` 可分配临时端口，启动后通过 `getPort()` 获取。
- `appContextFactory(...)`：由应用自行创建上下文，不会额外套用 Boot 的扫描与静态资源装配。

## 静态资源与响应

前端构建产物放在 `src/main/resources/META-INF/resources/`，或打包在依赖 JAR 的同名目录下。默认欢迎文件为 `index.html`，也可通过 `staticLocation(...)` 切换到其他有明确边界的类路径目录。

静态资源支持 GET、HEAD、MIME 类型及条件请求。Action 路由优先于资源处理；默认排除 `/api`、`/health` 及其子路径。资源规则匹配后找不到文件会返回 404，只有未被 Action 或资源规则处理的请求才继续交给 Servlet 链。默认排除路径不等于创建接口，也不是访问控制。

单页应用回退默认关闭，只对 `spaPaths` 指定且不带扩展名的路径生效；缺失的 JS、CSS 不会回退为首页。部署到非根上下文时，还需同步设置前端资源基路径。不要将私密配置放进公开资源目录。

返回值渲染是 Hasor Web 的内置机制：默认字符串输出文本，对象使用 JSON 渲染器，`null` 与 `void` 不生成默认响应体。使用 JSON 时，需要应用提供受支持的 JSON 库；不要把引入 Boot 容器依赖等同于引入 JSON 实现。默认引擎由 `hasor.render.*` 配置，详细用法参见 [Hasor Web 文档](../document/docs/guides/webmvc/overview.md)。

## Maven 打包

Maven 工程使用相同的启动类和容器依赖，在已有 JAR 工程的 `build/plugins` 中加入：

```xml
<plugin>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-maven-plugin</artifactId>
    <version>5.1.1-SNAPSHOT</version>
    <configuration>
        <mainClass>example.Application</mainClass>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

执行 `mvn package` 后通过 `java -jar target/应用名-版本.jar` 启动。未指定 `mainClass` 时，插件读取原 JAR 的 `Main-Class`。

Maven 插件默认替换原 JAR，与 Gradle 默认生成 `-boot.jar` 不同。配置 `classifier` 可以改变输出文件名，但当前实现仍将生成文件设为项目主构件，并非附加一个独立分类构件。Maven 插件自身依赖启动器，不需要照搬 Gradle 的 `bootLoader` 配置。

## 健康检查

Web 扩展默认提供 `GET /health`，健康时返回 HTTP 200 和 `{"status":"UP"}`。
自定义检查返回 false 或抛出异常时，总状态为 `DOWN`，HTTP 状态为 503。
响应禁止缓存，不包含异常堆栈或连接信息；检查项名称仍会公开，请勿放入敏感信息。

在应用 hconfig 中覆盖默认开关和路径：

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.boot.web.health>
        <enabled>true</enabled>
        <path>/internal/health</path>
    </hasor.boot.web.health>
</config>
```

设为 `false` 可关闭内置接口。默认值由 `boot-web-hconfig.xml` 承载，
也可使用 `HASOR_BOOT_WEB_HEALTH_ENABLED`、`HASOR_BOOT_WEB_HEALTH_PATH` 环境变量。
更改路径后不再注册默认的 `/health`。已存在的业务路由优先，关闭内置接口不会删除业务路由。

业务实现 `net.hasor.boot.web.health.HealthCheck`，再通过 `@Bean` 或
`binder.bindType(HealthCheck.class).nameWith("database").toInstance(...)` 注册到容器。
支持多个检查项，名称取容器绑定名称，为空时取绑定 ID；`@Bean` 默认使用方法名，也可通过 `@Bean("database")` 显式命名。名称必须非空且唯一；检查同步执行，实现应线程安全，并为数据库、网络等外部调用自行设置超时。
默认检查仅表示应用容器已启动，不会自动探测数据库或其他业务依赖。内置接口沿用 Hasor Web 的 JSON 渲染，需要应用提供 JSON 库。

完整示例见[启动文档](../document/docs/guides/deployment/boot-launcher.md)。

## 模块结构

| 模块 | 职责 |
| --- | --- |
| `hasor-boot-loader` | 可执行 JAR 启动器、应用类加载与嵌套 JAR 资源读取 |
| `hasor-boot-gradle-plugin` | Gradle 可执行 JAR 打包 |
| `hasor-boot-maven-plugin` | Maven 可执行 JAR 打包 |
| `hasor-boot` | 统一启动入口、应用容器创建、SPI 启动链及资源关闭 |
| `hasor-boot-web` | Web 启动扩展、Servlet 环境及内嵌容器 API |
| `hasor-boot-web-tomcat` | Tomcat 容器适配 |
| `hasor-boot-web-jetty` | Jetty 容器适配 |
| `hasor-boot-web-undertow` | Undertow 容器适配 |
| `test-web-containers` | 三种容器共享的测试代码与资源，仅供测试依赖，不发布 |

打包插件生成的主要结构：

```text
META-INF/MANIFEST.MF
net/hasor/boot/loader/       启动器
APP-INF/classes/            应用类与资源
APP-INF/lib/                运行依赖 JAR
```

清单中的 `Main-Class` 指向 `net.hasor.boot.loader.JarLauncher`，`Hasor-Main-Class` 指向应用入口。启动器加载应用目录和嵌套依赖，再调用应用的 `public static void main(String[] args)`。这些打包能力也可用于非 Web 应用。

源码中虽然保留了 `APP-INF/hasor/` 和 `Hasor-Class-Path` 常量，当前打包、启动流程并未将它们作为额外配置目录或扩展类路径使用。

## 源码构建与测试

以下命令在 Hasor 仓库根目录执行：

```bash
./gradlew :hasor-boot:test :hasor-boot-loader:test :hasor-boot-web:test \
  :hasor-boot-web-tomcat:test :hasor-boot-web-jetty:test :hasor-boot-web-undertow:test
```

共享测试位于 `test-web-containers/src/main/java` 和 `src/main/resources`，由各容器模块的具体测试类继承执行。仅运行共享模块自身的 `test` 不会执行三种容器的集成测试。该模块的 [README](test-web-containers/README.md) 记录了测试类加载环境及验证边界。

使用本地快照时，先发布所需模块，例如 Tomcat 组合：

```bash
./gradlew :hasor-core:publishToMavenLocal :hasor-web:publishToMavenLocal \
  :hasor-config:publishToMavenLocal :hasor-boot-loader:publishToMavenLocal \
  :hasor-boot:publishToMavenLocal :hasor-boot-web:publishToMavenLocal :hasor-boot-web-tomcat:publishToMavenLocal
./gradlew :hasor-boot-gradle-plugin:publishToMavenLocal
```

使用 Maven 打包时，另行发布 `hasor-boot-maven-plugin`。这些命令说明构建方式，不代表当前工作区已完成测试或发布。
