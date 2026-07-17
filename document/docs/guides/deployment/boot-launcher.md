---
id: boot-launcher
sidebar_position: 3
title: Boot 启动器
description: 使用 Hasor.run 编写普通 Hasor 应用的启动入口。
---

# Boot 启动器

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

`primarySources` 是 Hasor Boot 的主启动来源。它和普通 Module 的区别在于：

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
