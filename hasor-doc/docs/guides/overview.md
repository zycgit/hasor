---
id: overview
sidebar_position: 1
title: 介绍
description: Hasor 当前由 hasor-core、hasor-web、hasor-boot 三个核心模块组成。
---
# 介绍

Hasor 是一个面向 Java 应用的轻量级框架。当前代码仓库中的核心能力已经收束为三个模块：

- `hasor-core`：提供 IoC、AOP、作用域、事件、生命周期、配置和插件扩展能力。
- `hasor-web`：在 `hasor-core` 之上提供 Web MVC、请求映射、请求参数、响应渲染、文件上传和 Servlet 集成。
- `hasor-boot`：提供可执行 Fat Jar 打包能力，并为 Hasor Web 提供 Tomcat、Jetty、Undertow 三种内嵌容器实现。

因此这份文档也只围绕这三类能力组织。旧版本中曾经出现过、但当前仓库已经不再维护的扩展内容，不再作为当前 Hasor 文档的组成部分。

Hasor 的目标是让应用可以从一个小的核心容器开始，需要 Web 或可执行包能力时再按需加入对应模块。应用代码主要围绕 `Module`、`ApiBinder`、`AppContext` 展开：在 `Module` 中声明绑定关系和扩展点，通过 `Hasor.create().build(...)` 创建运行时上下文。

## 运行环境

从下个版本开始，Hasor 预计会进入 `5.0.0` 版本线，并且只支持 JDK 17 及以上版本。新项目建议直接使用 JDK 17+ 作为编译和运行环境。

## 特点

Hasor 的设计思想仍然是“微内核 + 插件”。`hasor-core` 保持较小的核心面，`hasor-web` 通过 `WebApiBinder` 扩展 Web 场景下的绑定 API，`hasor-boot` 则通过 Maven/Gradle 插件和 Boot Loader 解决可执行包运行问题。

如果只是普通 Java 应用，引入 `hasor-core` 即可。如果需要传统 Servlet Web 应用，引入 `hasor-web` 并配置 `RuntimeListener`、`RuntimeFilter`。如果希望直接运行一个 Fat Jar，再加入 `hasor-boot-maven-plugin` 和一个内嵌容器模块。
