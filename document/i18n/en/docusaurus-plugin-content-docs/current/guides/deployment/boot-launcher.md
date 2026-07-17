---
id: boot-launcher
sidebar_position: 3
title: Boot Launcher
description: Use Hasor.run to write the startup entry for ordinary Hasor applications.
---

# Boot Launcher

Ordinary applications use `Hasor.run(args, PrimarySource.class)` as the startup entry. The startup class usually implements `Module`; the `main` method starts the application, and `loadModule` declares beans and extension points.

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

`primarySources` are the main startup sources for Hasor Boot. They differ from ordinary modules in the following ways:

- A primary source is always registered as a Hasor bean.
- It is created by Hasor, so the type must provide an accessible no-argument constructor.
- It is created as a singleton, and the `AppContext` obtains it before full dependency injection is performed.
- If it implements `Module`, the same instance participates in the `loadModule`, `onStart`, and `onStop` lifecycle.
- When `loadModule` is called, the container is still in the module-configuration phase, so dependency injection has not yet been performed on the primary source.
- For the primary source itself, dependency injection happens after `loadModule` and before `onStart`.

A startup class can therefore handle module configuration and lifecycle logic at the same time:

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

## Startup Arguments

`Hasor.run` binds the `args` received by the `main` method into the container:

- `net.hasor.core.info.Arguments`
- A named `String[]` whose name is `Arguments.MAIN_ARGS`

Business beans or the primary source can inject `Arguments` directly:

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

## Shutdown Handling

By default, Hasor registers a JVM shutdown hook. When the process exits normally or receives `SIGTERM` or `SIGINT`, Hasor calls `AppContext.shutdown()` and executes `Module#onStop`.

```java
Hasor.run(args, DemoHasorBootApplication.class);
```

If an application needs to control shutdown by itself, disable automatic registration:

```java
AppContext appContext = Hasor.create()
        .registerShutdownHook(false)
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();

appContext.shutdown();
```

:::tip
`kill -0 <pid>` only checks whether the process exists and whether the current user has permission to signal it. It does not notify the process to exit. Common exit notifications are `kill <pid>` and `kill -15 <pid>`, both of which trigger the JVM shutdown hook.
:::
