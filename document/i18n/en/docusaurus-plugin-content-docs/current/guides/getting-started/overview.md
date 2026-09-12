---
id: overview
sidebar_position: 1
title: Getting Started
description: Understand the responsibilities of hasor-core, hasor-config, hasor-web, and hasor-boot.
---
# Getting Started

Hasor is a lightweight framework for Java applications. The current repository is organized by responsibility:

- `hasor-core`: IoC, AOP, scopes, events, lifecycle, configuration, and plugin extensions.
- `hasor-config`: optional declarative configuration with `@Configuration`, `@Bean`, and Web auto-configuration.
- `hasor-web`: Web MVC, request mapping, request parameters, response rendering, file uploads, and Servlet integration on top of `hasor-core`.
- `hasor-boot`: a unified application startup entry point and extension lifecycle, with companion modules for Web containers, the Loader, and packaging plugins.

`hasor-boot` is both the module-group directory name and the published name of the unified startup module. Ordinary Boot applications depend on `net.hasor:hasor-boot`; Web applications need one container module.

Hasor lets applications start with a small core container and add Web or executable packaging capabilities as needed. Application code centers on `Module`, `ApiBinder`, and `AppContext`: declare bindings and extensions in a `Module`, then create the runtime context through `Hasor.create().build(...)`.

## Runtime requirements

Hasor `@project.docsVersion@` requires JDK 17 or later. Build the source using the repository Gradle Wrapper.

## Features

Hasor follows a microkernel-and-plugins design. `hasor-core` keeps a small core surface; `hasor-web` extends binding APIs through `WebApiBinder`; Boot extends the runtime through SPI. Companion Maven/Gradle plugins and the Loader handle executable archives.

Ordinary Java applications can use `hasor-core` alone. Add [hasor-config](../core/conf/java-config.md) for annotation configuration. Traditional Servlet Web applications add `hasor-web` and configure `RuntimeListener` and `RuntimeFilter`. Use Maven or Gradle packaging plugins for executable Fat Jars, adding a container module only when an embedded Web server is needed.
