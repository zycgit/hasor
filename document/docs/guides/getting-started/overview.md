---
id: overview
sidebar_position: 1
title: 1. 入门
description: 从 Java 对象管理、注解配置、Web MVC 和应用启动需求选择 Hasor 模块，并完成容器的创建、调用和关闭。
---
# 1. 入门

{/* llms:start */}

Hasor 是一个嵌入 Java 应用的轻量级框架，负责对象创建、依赖注入、配置、AOP、事件和生命周期管理。应用可以从核心容器开始，按需加入注解配置、Web MVC 和应用启动模块。

核心概念有四个：`Module` 声明装配规则，`ApiBinder` 注册对象和扩展，`Hasor` 构建容器，`AppContext` 保存运行时环境并提供 Bean。业务代码使用容器创建或绑定的对象，容器关闭时结束相应的生命周期。

## 按需求选择模块

| 需求 | 起步依赖与入口 | 继续阅读 |
| --- | --- | --- |
| 普通 Java 程序需要对象管理和依赖注入 | `net.hasor:hasor-core`；`Module`、`Hasor.create().build(...)` | [快速上手](./quickstart.mdx) |
| 使用配置类和工厂方法声明 Bean | `net.hasor:hasor-config`；`@Configuration`、`@Bean`、`ApplicationBoot` | [注解配置](../core/conf/java-config.md) |
| 在已有 Servlet 容器中提供 HTTP 接口 | `net.hasor:hasor-web`；`WebModule`、`WebApiBinder`，安装 Web 运行环境 | [Web MVC](../webmvc/overview.md) |
| 统一启动应用，或启动内嵌 Web 服务 | `net.hasor:hasor-boot`；Web 应用额外选择一个容器模块 | [Boot 启动](../deployment/boot-launcher.md) |
| 构建可执行 Fat Jar | 配置 Maven 或 Gradle 打包插件；Loader 负责加载归档 | [工程配置](../deployment/project-config.md) |

这些能力可以组合。`hasor-config` 不自动提供 Web 容器；内嵌 Web 应用在 Tomcat、Jetty、Undertow 模块中选择一个。数据库访问由独立的 [dbVisitor](../data-access.md) 集成提供。

## 完成第一次调用

1. 从 `hasor-core` 开始，在 `Module.loadModule(ApiBinder)` 中调用 `bindType(...)` 声明绑定。
2. 将 Module 传给 `Hasor.create().build(module)`，得到 `AppContext`；通过 `getInstance(Service.class)` 获取并调用业务对象。
3. 应用退出时关闭上下文。配置模块负责注册规则，业务对象的使用放在容器完成装配之后。

[快速上手](./quickstart.mdx) 包含依赖和代码。选择注解配置时，用 `hasor.loadPackages` 明确扫描范围；选择 Web 时，先准备 Servlet 或 Boot 运行环境，再注册 Controller。

{/* llms:end */}

## 运行环境

Hasor `@project.docsVersion@` 需要 JDK 17 或以上版本。源码构建使用仓库自带的 Gradle Wrapper。

## 特点

Hasor 的设计思想仍然是“微内核 + 插件”。`hasor-core` 保持较小的核心面，`hasor-web` 通过 `WebApiBinder` 扩展 Web 场景下的绑定 API，Boot 通过 SPI 扩展运行环境，配套 Maven/Gradle 插件和 Loader 负责可执行包。

如果只是普通 Java 应用，引入 `hasor-core` 即可；希望使用注解配置时，再加入 [hasor-config](../core/conf/java-config.md)。传统 Servlet Web 应用引入 `hasor-web` 并配置 `RuntimeListener`、`RuntimeFilter`。可执行 Fat Jar 使用 Maven 或 Gradle 打包插件；只有需要内嵌 Web 服务时才加入容器模块。
