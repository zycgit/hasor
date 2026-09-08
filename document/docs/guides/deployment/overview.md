---
id: overview
sidebar_position: 1
title: 讲解
description: 了解 Hasor Boot 的打包模型、运行模型和可执行归档结构。
---

# 讲解

Hasor Boot 用来把普通 Hasor 应用打包成可以直接运行的 Fat Jar。开发阶段可以直接运行 `main` 方法；发布时通过 Maven 或 Gradle 插件打包，最终使用 `java -jar app.jar` 启动。

它主要解决两件事：

- 构建期：把应用 class、运行期依赖和启动入口重新组织成 Hasor Boot 可执行归档。
- 运行期：通过 Hasor Boot Loader 创建应用 ClassLoader，并从嵌套 jar 中加载类、资源和 Hasor 扩展描述。

## 适用场景

普通 Java 应用只需要 `hasor-core` 和 `hasor-boot-maven-plugin`。启动入口使用 `Hasor.run(args, PrimarySource.class)`，打包后由 Hasor Boot Loader 调用真实入口。

Web 应用需要再加入 `hasor-web` 和一个内嵌容器模块，例如 `hasor-boot-web-tomcat`。启动入口使用 `WebServers.run(args, RootModule.class)`，由 Hasor 创建 Web 容器并自动装配 `RuntimeListener`、`RuntimeFilter`。

## 运行流程

可执行包启动时，JVM 首先执行 Manifest 中的 `Main-Class`。这个类不是业务应用入口，而是 Hasor Boot Loader：

```text
Main-Class: net.hasor.boot.loader.JarLauncher
Hasor-Main-Class: net.hasor.demo.boot.DemoHasorBootApplication
```

`JarLauncher` 会读取 `Hasor-Main-Class`，创建应用 ClassLoader，然后调用真实业务入口的 `main` 方法。

## 归档结构

Hasor Boot 可执行包主要使用下面的布局，其中 `APP-INF/hasor/` 是预留配置目录：

```text
META-INF/MANIFEST.MF
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

运行时的 classpath 由 Boot Loader 组装：

- `APP-INF/classes/` 作为应用 classpath。
- `APP-INF/lib/*.jar` 作为嵌套依赖 jar。
- 嵌套 jar 中的 class、资源和 `META-INF/hasor.schemas` 会通过 Cobble Loader 读取。

应用 classes 目录作为嵌套目录参与加载，即使归档没有显式的目录条目也可以建立目录视图。空资源名查询会返回应用目录和嵌套依赖的根 URL，供 classpath 资源发现使用。应用资源路径仍相对于 classpath 根，例如读取 `hconfig.xml`，无需写入 `APP-INF/classes/` 前缀。

## 章节路径

完整接入通常按下面顺序完成：

1. 在 [工程配置](./project-config.md) 中引入依赖并配置 Maven 或 Gradle 打包。
2. 普通应用使用 [Boot 启动器](./boot-launcher.md) 编写启动入口。
3. Web 应用使用 [Web 启动器](./web-launcher.md) 编写内嵌容器启动入口。
