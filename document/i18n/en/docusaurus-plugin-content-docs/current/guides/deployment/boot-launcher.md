---
id: boot-launcher
sidebar_position: 3
title: 5.2 Boot Launcher
description: Unified Boot startup, Core entry points, lifecycle, and health checks.
---

# 5.2 Boot Launcher

New applications should use `net.hasor.boot.Boot`. Ordinary and Web applications share the same entry point:

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

Ordinary applications depend on `net.hasor:hasor-boot:@project.docsVersion@`; Web applications need one container module. Close one-shot applications after completing their work instead of calling `join()`. See [Project Configuration](./project-config.md).

## Core entry point

Ordinary applications use `Hasor.run(args, PrimarySource.class)`. The startup class usually also implements `Module`: `main` starts the application, while `loadModule` declares Beans and extension points.

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

`Hasor.run(args, DemoHasorBootApplication.class)` is equivalent to:

```java
Hasor.create()
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();
```

## primarySources

`primarySources` are the primary startup sources for Hasor Core. They differ from ordinary Modules as follows:

- A primary source is always registered as a Hasor Bean.
- Hasor creates it, so the type must provide an accessible no-argument constructor.
- It is created as a singleton; after `AppContext` obtains it, full dependency injection is performed.
- If it implements `Module`, the same instance participates in `loadModule`, `onStart`, and `onStop`.
- `loadModule` runs during module configuration, before dependency injection on the primarySource.
- For the primarySource itself, injection occurs after `loadModule` and before `onStart`.

A startup class can therefore handle both module configuration and startup lifecycle logic:

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

## Startup arguments

`Hasor.run` binds the `args` received by `main` into the container:

- `net.hasor.core.info.Arguments`
- A `String[]` named `Arguments.MAIN_ARGS`

Business Beans and primarySources can inject `Arguments` directly:

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

## Shutdown handling

Hasor registers a JVM shutdown hook by default. On normal process exit, `SIGTERM`, or `SIGINT`, Hasor calls `AppContext.shutdown()` and executes `Module#onStop`.

```java
Hasor.run(args, DemoHasorBootApplication.class);
```

Disable automatic registration if the application needs to control shutdown itself:

```java
AppContext appContext = Hasor.create()
        .registerShutdownHook(false)
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();

appContext.shutdown();
```

:::tip
`kill -0 <pid>` only checks whether the process exists and the current user has permission; it does not request termination. Typical termination commands are `kill <pid>` and `kill -15 <pid>`, which trigger the JVM shutdown hook.
:::
## Boot extensions and lifecycle

`new Boot().sources(...).arguments(...).hconfigFile(...).property(...).start()` supports configuration in code; property overrides file properties.
`BootApplication.getAppContext()` returns the application container. `close()` closes extension resources in reverse order before closing the container; `join()` waits for shutdown.
Boot discovers `BootExtension` through SPI and sorts extensions by order and class name. Extensions wrap the next BootLauncher stage and pass on the environment and modules.
The Web extension creates the same application container in the Servlet context callback; it does not start a separate business container.
Extensions should invoke the next stage only once, register cleanup through onClose, and release resources they acquired if startup fails.

## Health checks

Boot Web provides `GET /health` by default, prefixed by the configured context path.
Use `hasor.boot.web.health.enabled` to enable or disable it and `hasor.boot.web.health.path` to change its address.
The corresponding environment variables are `HASOR_BOOT_WEB_HEALTH_ENABLED` and `HASOR_BOOT_WEB_HEALTH_PATH`.

Declare checks in a configuration class:
```java
@Bean
public net.hasor.boot.web.health.HealthCheck database() {
    return () -> databaseAvailable();
}
```

`databaseAvailable()` is application-defined probe logic. The interface only declares `boolean check() throws Exception`; no `name()` is required.
Names come from container binding names, falling back to binding IDs when empty. Ordinary `@Bean` methods use their method names; explicit `@Bean("db")` is also supported. Names must be nonempty and unique; no suffix is stripped.

Every request runs all checks synchronously without short-circuiting on failure. All passing checks return HTTP 200; any false result or exception returns HTTP 503:
`{"status":"DOWN","checks":{"database":"DOWN"}}`。
Without business checks, only the application container startup state is checked. Grouping, selectable aggregation strategies, and result caching are not currently supported.
Implementations must be thread-safe and set their own connection-pool, network, and other timeouts. Responses do not expose exception details, but check names are public; restrict access in production.
The default endpoint requires an available JSON renderer. It is also inaccessible when the HTTP listener is disabled.
