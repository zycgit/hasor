---
id: overview
sidebar_position: 0
title: Web MVC
description: Build Servlet Web MVC applications with hasor-web.
---

# Web MVC

`hasor-web` provides Servlet Web MVC capabilities on top of `hasor-core`. It can run in a traditional Servlet container or through an embedded container started by Hasor Boot.

This chapter covers common Web application capabilities:

- Web application entry points and startup modes.
- Request mapping, HTTP methods, and request object access.
- Parameter reading, parameter groups, and type conversion.
- Response rendering, Content-Type, templates, and JSON output.
- Request validation, upload, interceptors, and J2EE compatibility.

Embedded-container APIs are in `net.hasor.boot.web`; implementations are provided by the `hasor-boot-web-tomcat`, `hasor-boot-web-jetty`, and `hasor-boot-web-undertow` modules. See [Java Configuration](../core/conf/java-config.md) for Controller scanning, static resources, CORS, and JSON customization.
