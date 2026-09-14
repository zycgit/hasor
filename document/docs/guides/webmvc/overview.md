---
id: overview
sidebar_position: 0
title: 4. Web MVC
description: 使用 hasor-web 构建 Servlet Web MVC 应用。
---

# 4. Web MVC

`hasor-web` 在 `hasor-core` 之上提供 Servlet Web MVC 能力。它既可以运行在传统 Servlet 容器中，也可以通过 Hasor Boot 启动内嵌容器。

内嵌容器公共 API 位于 `net.hasor.boot.web`，实现由 `hasor-boot-web-tomcat`、`hasor-boot-web-jetty`、`hasor-boot-web-undertow` 提供。注解 Controller 扫描、静态资源、CORS 和 JSON 配置入口见 [Java 注解配置](../core/conf/java-config.md)，上下文创建和启动方式见 [Web 启动器](../deployment/web-launcher.md)。

本章节覆盖 Web 应用开发中的常用能力：

- Web 开发入口和启动方式。
- 请求映射、HTTP 方法和请求对象获取。
- 参数读取、参数分组和类型转换。
- 结果渲染、Content-Type、模板和 JSON 输出。
- 请求验证、上传、拦截器和 J2EE 兼容。
