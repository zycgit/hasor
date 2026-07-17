---
id: overview
sidebar_position: 1
title: Overview
description: Understand the Hasor Boot packaging model, runtime model, and executable archive layout.
---

# Overview

Hasor Boot packages a regular Hasor application as a directly executable fat jar. During development, run the `main` method directly. For release, let the Maven plugin repackage the application and start it with `java -jar app.jar`.

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
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

The runtime classpath is assembled by Boot Loader:

- `APP-INF/classes/` is used as the application classpath.
- `APP-INF/lib/*.jar` is used as nested dependency jars.
- Classes, resources, and `META-INF/hasor.schemas` inside nested jars are read through Cobble Loader.

## Reading Path

A complete setup usually follows this order:

1. Add dependencies and Maven packaging in [Project Configuration](./project-config.md).
2. Use [Boot Launcher](./boot-launcher.md) for ordinary applications.
3. Use [Web Launcher](./web-launcher.md) for Web applications with embedded containers.
