---
id: overview
sidebar_position: 1
title: 介绍
description: 了解 hasor-core、hasor-config、hasor-web 和 hasor-boot 的职责。
---
# 介绍

Hasor 是一个面向 Java 应用的轻量级框架。当前代码仓库按以下职责组织：

- `hasor-core`：提供 IoC、AOP、作用域、事件、生命周期、配置和插件扩展能力。
- `hasor-config`：提供 `@Configuration`、`@Bean`、注解 AOP 和 Web 自动配置，是可选的声明式配置入口。
- `hasor-web`：在 `hasor-core` 之上提供 Web MVC、请求映射、请求参数、响应渲染、文件上传和 Servlet 集成。
- `hasor-boot`：提供可执行 Fat Jar 打包能力，并为 Hasor Web 提供 Tomcat、Jetty、Undertow 三种内嵌容器实现。

`hasor-boot` 是模块组，包含 Loader、Maven/Gradle 插件、`hasor-boot-web` 公共层和三种容器实现，不是需要整体引入的单一依赖。

Hasor 的目标是让应用可以从一个小的核心容器开始，需要 Web 或可执行包能力时再按需加入对应模块。应用代码主要围绕 `Module`、`ApiBinder`、`AppContext` 展开：在 `Module` 中声明绑定关系和扩展点，通过 `Hasor.create().build(...)` 创建运行时上下文。

## 运行环境

当前源码版本为 `5.0.2-SNAPSHOT`，Java 编译目标为 17，运行应用需要 JDK 17 或以上版本。源码构建使用仓库自带的 Gradle Wrapper。

## 特点

Hasor 的设计思想仍然是“微内核 + 插件”。`hasor-core` 保持较小的核心面，`hasor-web` 通过 `WebApiBinder` 扩展 Web 场景下的绑定 API，`hasor-boot` 则通过 Maven/Gradle 插件和 Boot Loader 解决可执行包运行问题。

如果只是普通 Java 应用，引入 `hasor-core` 即可；希望使用注解配置时，再加入 [hasor-config](../core/conf/java-config.md)。传统 Servlet Web 应用引入 `hasor-web` 并配置 `RuntimeListener`、`RuntimeFilter`。可执行 Fat Jar 使用 Maven 或 Gradle 打包插件；只有需要内嵌 Web 服务时才加入容器模块。
