---
id: overview
sidebar_position: 1
title: 1. Getting Started
description: Choose Hasor modules for object management, annotation configuration, Web MVC and application startup, then create, use and close a container.
---
# 1. Getting Started

{/* llms:start */}

Hasor is a lightweight framework embedded in Java applications. It manages object creation, dependency injection, configuration, AOP, events and lifecycle. Start with its core container and add annotation configuration, Web MVC and application startup modules as needed.

Four concepts connect the API: `Module` declares assembly rules, `ApiBinder` registers objects and extensions, `Hasor` builds the container, and `AppContext` holds the runtime and resolves beans. Application code uses objects created or bound by the container; closing the context ends their managed lifecycle.

## Choose modules by task

| Requirement | Dependency and entry point | Read next |
| --- | --- | --- |
| Object management and injection in ordinary Java | `net.hasor:hasor-core`; `Module`, `Hasor.create().build(...)` | [Quick start](./quickstart.mdx) |
| Declare beans with configuration classes and factory methods | `net.hasor:hasor-config`; `@Configuration`, `@Bean`, `ApplicationBoot` | [Annotation configuration](../core/conf/java-config.md) |
| Serve HTTP endpoints in an existing Servlet container | `net.hasor:hasor-web`; `WebModule`, `WebApiBinder`, and a Web runtime | [Web MVC](../webmvc/overview.md) |
| Start an application or an embedded Web server | `net.hasor:hasor-boot`; add one container module for Web applications | [Boot startup](../deployment/boot-launcher.md) |
| Build an executable Fat Jar | Configure the Maven or Gradle packaging plugin; the Loader loads the archive | [Project configuration](../deployment/project-config.md) |

These capabilities can be combined. `hasor-config` does not supply a Web container; embedded Web applications choose one of the Tomcat, Jetty or Undertow modules. Database access is provided by the separate [dbVisitor integration](../data-access.md).

## Complete a first call

1. Start with `hasor-core` and declare bindings with `bindType(...)` in `Module.loadModule(ApiBinder)`.
2. Pass the Module to `Hasor.create().build(module)` to obtain an `AppContext`. Resolve a service with `getInstance(Service.class)` and call it.
3. Close the context when the application stops. Modules register assembly rules; use business objects after container assembly completes.

The [quick start](./quickstart.mdx) provides dependencies and code. For annotation configuration, set the scan scope with `hasor.loadPackages`. For Web applications, establish a Servlet or Boot runtime before registering controllers.

{/* llms:end */}

## Runtime requirements

Hasor `@project.docsVersion@` requires JDK 17 or later. Build the source using the repository Gradle Wrapper.

## Features

Hasor follows a microkernel-and-plugins design. `hasor-core` keeps a small core surface; `hasor-web` extends binding APIs through `WebApiBinder`; Boot extends the runtime through SPI. Companion Maven/Gradle plugins and the Loader handle executable archives.

Ordinary Java applications can use `hasor-core` alone. Add [hasor-config](../core/conf/java-config.md) for annotation configuration. Traditional Servlet Web applications add `hasor-web` and configure `RuntimeListener` and `RuntimeFilter`. Use Maven or Gradle packaging plugins for executable Fat Jars, adding a container module only when an embedded Web server is needed.
