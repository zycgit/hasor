---
id: boot-launcher
sidebar_position: 3
title: Boot 启动器
description: 统一 Boot 入口、Core 启动方式、生命周期与健康检查。
---

# Boot 启动器

新应用推荐使用 `net.hasor.boot.Boot`，普通应用与 Web 应用使用同一个入口：

```java
public class Application {
    public static void main(String[] args) throws Exception {
        try (net.hasor.boot.BootApplication app =
                     net.hasor.boot.Boot.run(args, Application.class)) {
            app.join();
        }
    }
}
```

普通应用依赖 `net.hasor:hasor-boot:5.1.1-SNAPSHOT`；Web 应用引入一个容器模块即可。一次性任务完成后关闭应用，不调用 `join()`。参见[工程配置](./project-config.md)。

## Core 启动入口

普通应用使用 `Hasor.run(args, PrimarySource.class)` 作为启动入口。启动类通常同时实现 `Module`，`main` 方法负责启动，`loadModule` 方法负责声明 Bean 和扩展点。

```java
import net.hasor.core.ApiBinder;
import net.hasor.core.Hasor;
import net.hasor.core.Module;

public class DemoHasorBootApplication implements Module {
    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class)
                .toInstance(new HelloService("Hasor Boot"));
    }
}
```

`Hasor.run(args, DemoHasorBootApplication.class)` 等价于：

```java
Hasor.create()
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();
```

## primarySources

`primarySources` 是 Hasor Core 的主启动来源。它和普通 Module 的区别在于：

- 它一定会被注册为 Hasor Bean。
- 它由 Hasor 创建，因此类型需要提供可访问的无参构造方法。
- 它会以单例方式创建，并由 `AppContext` 获取后执行完整依赖注入。
- 如果它实现了 `Module`，同一个实例会参与 `loadModule`、`onStart`、`onStop` 生命周期。
- `loadModule` 被调用时还处于模块配置阶段，此时不会对 primarySource 执行依赖注入。
- 对 primarySource 自身而言，依赖注入会发生在 `loadModule` 之后、`onStart` 之前。

因此一个启动类可以同时承担模块配置和启动生命周期逻辑：

```java
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.core.Module;
import net.hasor.core.info.Arguments;

public class DemoHasorBootApplication implements Module {
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
        apiBinder.bindType(HelloService.class)
                .toInstance(new HelloService("Hasor Boot"));
    }

    @Override
    public void onStart(AppContext appContext) {
        this.helloService.sayHello(this.arguments);
    }
}
```

## 启动参数

`Hasor.run` 会把 `main` 方法收到的 `args` 绑定到容器中：

- `net.hasor.core.info.Arguments`
- 命名为 `Arguments.MAIN_ARGS` 的 `String[]`

业务 Bean 或 primarySource 可以直接注入 `Arguments`：

```java
import net.hasor.core.Inject;
import net.hasor.core.info.Arguments;

public class HelloService {
    @Inject
    private Arguments arguments;

    public String sayHello() {
        return "hello args=" + this.arguments;
    }
}
```

## 关闭处理

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
## Boot 扩展与生命周期

`new Boot().sources(...).arguments(...).hconfigFile(...).property(...).start()` 支持代码配置，property 覆盖文件属性。
`BootApplication.getAppContext()` 获取应用容器，`close()` 逆序关闭扩展资源再关闭容器，`join()` 等待关闭。
Boot 通过 SPI 发现 `BootExtension`，按 order 和类名排列；扩展包装下一阶段 BootLauncher，传递环境与模块。
Web 扩展在 Servlet 上下文回调中创建同一个应用容器；不是再单独启动一个业务容器。
扩展应仅调用下一阶段一次，通过 onClose 注册清理，启动失败时释放自己取得的资源。

## 健康检查

Boot Web 默认提供 `GET /health`，地址包含配置的上下文路径前缀。
`hasor.boot.web.health.enabled` 控制开关，`hasor.boot.web.health.path` 修改地址；
对应环境变量为 `HASOR_BOOT_WEB_HEALTH_ENABLED`、`HASOR_BOOT_WEB_HEALTH_PATH`。

在配置类中声明检查项：
```java
@Bean
public net.hasor.boot.web.health.HealthCheck database() {
    return () -> databaseAvailable();
}
```

`databaseAvailable()` 是业务自行实现的探测逻辑。接口只有 `boolean check() throws Exception`，不需要 `name()`。
名称取容器绑定名称，为空时取绑定 ID：普通 `@Bean` 使用方法名，也可写 `@Bean("db")`。名称必须非空且唯一，不裁剪后缀。

每次请求同步执行全部检查，不因一个失败而短路。全部通过返回 HTTP 200；任意 false 或异常返回 HTTP 503：
`{"status":"DOWN","checks":{"database":"DOWN"}}`。
没有业务检查项时仅判断应用容器是否启动。当前没有分组、可选汇总策略或结果缓存。
实现需线程安全并自行设置连接池、网络等超时。响应不暴露异常细节，但检查名称公开；生产环境应限制访问。
默认接口需要可用 JSON 渲染器；关闭 HTTP 监听后接口也不可访问。
