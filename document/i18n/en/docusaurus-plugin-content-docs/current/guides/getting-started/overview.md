---
id: overview
sidebar_position: 1
title: Introduction
description: Hasor currently consists of hasor-core, hasor-web, and hasor-boot.
---
# Introduction

Hasor is a lightweight framework for Java applications. The current repository is centered on three modules:

- `hasor-core`: IoC, AOP, scopes, events, lifecycle hooks, configuration, and plugin extension points.
- `hasor-web`: Web MVC, request mapping, request parameters, response rendering, file upload, and Servlet integration on top of `hasor-core`.
- `hasor-boot`: executable Fat Jar packaging plus embedded Tomcat, Jetty, and Undertow support for Hasor Web.

This documentation is organized around those three capabilities. Older extension content that is no longer maintained in the current repository is no longer part of the Hasor documentation surface.

Most Hasor applications are built around `Module`, `ApiBinder`, and `AppContext`: declare bindings and extension points in a `Module`, then create the runtime context with `Hasor.create().build(...)`.

## Runtime Requirements

Starting with the next version, Hasor is expected to move to the `5.0.0` line and support only JDK 17 or later. New projects should use JDK 17+ for both compilation and runtime.

## Features

Hasor still follows a "microkernel + plugins" model. `hasor-core` keeps the core container small, `hasor-web` extends the binding API through `WebApiBinder`, and `hasor-boot` handles executable archive packaging and runtime loading.

For ordinary Java applications, use `hasor-core`. For Servlet Web applications, add `hasor-web` and configure `RuntimeListener` and `RuntimeFilter`. For executable Fat Jar applications, add the Hasor Boot packaging plugin and one embedded container module.
