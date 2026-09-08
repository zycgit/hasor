---
id: overview
sidebar_position: 1
title: Introduction
description: Learn the roles of hasor-core, hasor-config, hasor-web, and hasor-boot.
---
# Introduction

Hasor is a lightweight framework for Java applications, organized by these responsibilities:

- `hasor-core`: IoC, AOP, scopes, events, lifecycle hooks, configuration, and plugin extension points.
- `hasor-config`: optional Java configuration, Bean factories, annotation AOP, and Web auto-configuration.
- `hasor-web`: Web MVC, request mapping, request parameters, response rendering, file upload, and Servlet integration on top of `hasor-core`.
- `hasor-boot`: executable Fat Jar packaging plus embedded Tomcat, Jetty, and Undertow support for Hasor Web.

`hasor-boot` is a group of modules: the loader, Maven/Gradle plugins, the shared Web API, and individual embedded containers.

Most Hasor applications are built around `Module`, `ApiBinder`, and `AppContext`: declare bindings and extension points in a `Module`, then create the runtime context with `Hasor.create().build(...)`.

## Runtime Requirements

The current source version is `5.0.2-SNAPSHOT`. Java compilation targets version 17; applications require JDK 17 or later. Build the repository with its Gradle Wrapper.

## Features

Hasor still follows a "microkernel + plugins" model. `hasor-core` keeps the core container small, `hasor-web` extends the binding API through `WebApiBinder`, and `hasor-boot` handles executable archive packaging and runtime loading.

For ordinary Java applications, use `hasor-core`; add [hasor-config](../core/conf/java-config.md) for annotation configuration. For Servlet Web applications, add `hasor-web` and configure `RuntimeListener` and `RuntimeFilter`. Fat Jars need a Maven or Gradle packaging plugin; only embedded Web applications need a container module.
