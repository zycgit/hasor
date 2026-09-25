---
id: overview
sidebar_position: 1
title: 5. Boot Startup
description: Understand the Hasor Boot packaging model, runtime model, and executable archive layout.
---

# 5. Boot Startup

Hasor Boot packages a regular Hasor application as an executable fat jar. During development, run `main` directly. Use the Maven or Gradle plugin for packaging and start the archive with `java -jar app.jar`.

It handles two responsibilities:

- Build time: reorganize application classes, runtime dependencies, and the startup entry into a Hasor Boot executable archive.
- Runtime: create the application `ClassLoader` through Hasor Boot Loader and load classes, resources, and Hasor extension descriptors from nested jars.

## Use Cases

Ordinary Java applications need `hasor-core` and `hasor-boot-maven-plugin`. The startup entry uses `Hasor.run(args, PrimarySource.class)`, and the packaged archive is launched by Hasor Boot Loader.

Web applications also need `hasor-web` and one embedded container module, such as `hasor-boot-web-tomcat`. The startup entry uses `WebServers.run(args, RootModule.class)`, which creates the Web container and registers `RuntimeListener` and `RuntimeFilter`.

## Runtime Flow

When the executable archive starts, the JVM first executes the `Main-Class` in the manifest. This class is not the business application entry; it is Hasor Boot Loader:

```text
Main-Class: net.hasor.boot.loader.JarLauncher
Hasor-Main-Class: net.hasor.demo.boot.DemoHasorBootApplication
```

`JarLauncher` reads `Hasor-Main-Class`, creates the application `ClassLoader`, and invokes the real business `main` method.

## Archive Layout

Hasor Boot executable archives mainly use the following layout. `APP-INF/hasor/` is a reserved configuration directory.

```text
META-INF/MANIFEST.MF
net/hasor/boot/loader/
net/hasor/boot/loader/internal/cobble/logging/
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

The runtime classpath is assembled by Boot Loader:

- `APP-INF/classes/` is used as the application classpath.
- `APP-INF/lib/*.jar` is used as nested dependency jars.
- Classes, resources, and `META-INF/hasor.schemas` inside nested jars are read through Boot Loader.

Starting with **5.3.0**, `hasor-boot-loader` embeds the required Cobble logging classes at build time, relocates them to `net.hasor.boot.loader.internal.cobble`, and removes unrelated classes.
Project dependencies and Maven/Gradle publications use this self-contained Loader JAR without an additional classifier. Both packaging plugins use it, so the launcher can log before application dependencies are loaded and requires no additional application Cobble dependency.
The application's own Cobble stays in `APP-INF/lib/` with its original package names, independently of the loader's embedded version. Hasor Core, Config, and Boot within the application still require compatible Cobble versions.

## Reading Path

A complete setup usually follows this order:

1. Add dependencies and Maven or Gradle packaging in [Project Configuration](./project-config.md).
2. Use [Boot Launcher](./boot-launcher.md) for ordinary applications.
3. Use [Web Launcher](./web-launcher.md) for Web applications with embedded containers.

## Resource Roots

The loader treats `APP-INF/classes/` as a nested directory, even if explicit directory entries are missing. Empty resource-name queries expose application and nested-library root URLs for classpath discovery. Resource names remain relative to the classpath root: use `hconfig.xml`, without an `APP-INF/classes/` prefix.
