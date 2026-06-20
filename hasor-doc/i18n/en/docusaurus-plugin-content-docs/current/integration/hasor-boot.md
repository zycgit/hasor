---
id: hasor-boot
sidebar_position: 5
title: Hasor Boot 可执行包
description: 使用 Hasor Boot 打包可执行 Fat Jar，并通过 Hasor.run 启动 Hasor 应用。
---

# Hasor Boot 可执行包

Hasor Boot 用来把普通 Hasor 应用打包成可以直接执行的 Fat Jar。它负责两件事：

- 构建期参与 Maven 打包，将应用 class 和运行期依赖重新组织到可执行归档中。
- 运行期通过 Hasor Boot Loader 创建应用 ClassLoader，并从嵌套 jar 中加载类和资源。

## 启动入口

Hasor Boot 推荐使用 `Hasor.run(args, PrimarySource.class)` 作为启动入口。

```java
public class DemoHasorBootApplication implements Module {
    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class).toInstance(new HelloService("Hasor Boot"));
    }
}
```

`Hasor.run` 等价于下面的构建方式：

```java
Hasor.create()
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();
```

## primarySources

`primarySources` 是 Hasor Boot 的主启动来源。它和普通 Module 的区别在于：

- 它一定会被注册为 Hasor Bean。
- 它由 Hasor 创建，因此类型需要提供可访问的无参构造方法。如果已经定义了其它构造方法，也需要保留一个无参构造方法。
- 它会以单例方式创建，并由 `AppContext` 获取后执行完整依赖注入。
- 如果它实现了 `Module`，同一个实例也会参与 `loadModule`、`onStart`、`onStop` 生命周期。
- `loadModule` 被调用时还处于模块配置阶段，此时不会对 primarySource 执行依赖注入，因此不要在 `loadModule` 中使用 `@Inject` 字段或依赖注入结果。
- 对 primarySource 自身而言，依赖注入会发生在 `loadModule` 之后、`onStart` 之前。

因此一个启动类可以同时承担模块配置和启动生命周期逻辑：

```java
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.core.Module;
import net.hasor.core.info.Arguments;

public class DemoHasorBootApplication implements Module {
    private static final Logger logger = LoggerFactory.getLogger(DemoHasorBootApplication.class);

    @Inject
    private Arguments arguments;
    @Inject
    private HelloService helloService;

    public DemoHasorBootApplication() {
    }

    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class).toInstance(new HelloService("Hasor Boot"));
    }

    @Override
    public void onStart(AppContext appContext) {
        logger.info(this.helloService.sayHello(this.arguments));
    }

    @Override
    public void onStop(AppContext appContext) {
        logger.info(this.helloService.sayGoodbye(this.arguments));
    }
}
```

## 启动参数

`Hasor.run` 会把 `main` 方法的 `args` 绑定到容器中：

- `net.hasor.core.info.Arguments`
- 命名为 `Arguments.MAIN_ARGS` 的 `String[]`

业务 Bean 或 primarySource 可以直接注入 `Arguments`：

```java
public class HelloService {
    public String sayHello(Arguments args) {
        return "hello Hasor Boot args=" + args;
    }
}
```

## Web 启动

Hasor Web 应用可以使用 `WebServers.run(args, RootModule.class)` 启动内嵌容器。应用入口不需要创建 `TomcatWebServer`、`JettyWebServer` 或 `UndertowWebServer`，`WebServers` 会从 `hasor.http` 配置读取启动参数，并在 classpath 中自动发现可用的容器实现。

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
        apiBinder.loadMappingTo(HelloWebAction.class);
    }
}
```

`hasor-web` 的 `web-hconfig.xml` 中已经提供了默认的 `hasor.http` 小节，应用只需要在自己的 `hconfig.xml` 中覆盖少数运行参数。Hasor Web 的 `RuntimeFilter` 名称、匹配路径、boot 入口配置文件以及 ServletContext 静态资源根目录由框架内部固定处理，不需要暴露为 HTTP 配置项。`hasor.layout.layoutPath` 和 `hasor.layout.templatePath` 只用于渲染模板查找，不作为内嵌容器的静态资源根目录。

```xml
<config>
    <hasor>
        <http>
            <host>0.0.0.0</host>
            <port>8080</port>
            <contextPath>/</contextPath>
        </http>
    </hasor>
</config>
```

`server` 可以按名称显式指定；如果为空，会通过 Java SPI 自动发现 classpath 中的 `WebServerProvider`。官方容器名称为 `tomcat`、`jetty`、`undertow`。

```xml
<server>tomcat</server>
```

## Shutdown Hook

默认情况下，Hasor 会注册 JVM shutdown hook。当进程正常退出、收到 `SIGTERM` 或 `SIGINT` 时，Hasor 会触发 `AppContext.shutdown()`，并执行 `Module#onStop`。

```java
Hasor.run(args, DemoHasorBootApplication.class);
```

如果应用需要自己控制关闭时机，可以关闭自动注册：

```java
AppContext appContext = Hasor.create()
        .registerShutdownHook(false)
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();

appContext.shutdown();
```

:::tip
`kill -0 <pid>` 只用于探测进程是否存在和当前用户是否有权限，不会通知进程退出。常见的退出通知是 `kill <pid>` 或 `kill -15 <pid>`，它们会触发 JVM shutdown hook。
:::

## Maven 打包

Hasor Boot Maven 插件会在 `package` 阶段把普通 jar 重打包成 Hasor Boot 可执行归档。

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.4.2</version>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
        <plugin>
            <groupId>net.hasor</groupId>
            <artifactId>hasor-boot-maven-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

如果不想通过 `maven-jar-plugin` 写入 `Main-Class`，也可以直接配置插件参数：

```xml
<configuration>
    <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
</configuration>
```

构建并运行：

```bash
mvn -pl demo-hasor-boot/demo-hasor-boot-basic -am package
java -jar demo-hasor-boot/demo-hasor-boot-basic/target/demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar demo
```

## 归档结构

Hasor Boot 可执行包主要使用下面的布局，其中 `APP-INF/hasor/` 是预留配置目录：

```text
META-INF/MANIFEST.MF
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

Manifest 中的 `Main-Class` 会指向 Hasor Boot Loader，真实应用入口会写入 `Hasor-Main-Class`：

```text
Main-Class: net.hasor.boot.loader.JarLauncher
Hasor-Main-Class: net.hasor.demo.boot.DemoHasorBootApplication
```

运行时，`JarLauncher` 会创建应用 ClassLoader：

- `APP-INF/classes/` 作为应用 classpath。
- `APP-INF/lib/*.jar` 作为嵌套依赖 jar。
- 使用 Cobble Loader 读取嵌套 jar 中的 class、资源和 `META-INF/hasor.schemas`。

这样应用可以通过一条命令启动：

```bash
java -jar demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar
```
