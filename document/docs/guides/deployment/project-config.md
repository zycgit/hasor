---
id: project-config
sidebar_position: 2
title: 工程配置
description: 配置 Hasor Boot 依赖以及 Maven、Gradle 可执行包插件。
---

# 工程配置

Hasor Boot 的工程配置分为两部分：运行期依赖和 Maven/Gradle 打包插件。普通应用只需要核心依赖和打包插件；Web 应用还需要选择一个内嵌容器模块。以下示例使用 Hasor `@project.docsVersion@`。

## 普通应用依赖

使用统一 `Boot.run(...)` 的普通应用引入 `hasor-boot`：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

如果只是在开发阶段直接运行 `main` 方法，不需要额外运行期依赖。需要打包成可执行 Fat Jar 时，在 Maven 中配置 `hasor-boot-maven-plugin`。

## Web 应用依赖

Web 应用需要 `hasor-web`，并选择一个内嵌容器模块：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

容器模块已传递引入 Boot、Config、Web。必须只引入一种容器，零个或多个 SPI 实现都会报错。默认 JSON 渲染需业务提供 JSON 库，详见 [JSON 渲染](../webmvc/response/json_render.md)。

内嵌容器模块可以按需要替换为：

- `hasor-boot-web-tomcat`
- `hasor-boot-web-jetty`
- `hasor-boot-web-undertow`

## Maven 打包插件

Hasor Boot Maven 插件会在 `package` 阶段把普通 jar 重打包成可执行归档。

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.4.2</version>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
        <plugin>
            <groupId>net.hasor</groupId>
            <artifactId>hasor-boot-maven-plugin</artifactId>
            <version>@project.docsVersion@</version>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

如果不想通过 `maven-jar-plugin` 写入 `Main-Class`，也可以直接配置 Hasor Boot 插件参数：

```xml
<configuration>
    <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
</configuration>
```

## 构建和运行

执行 Maven package 后，target 目录中会生成可以直接运行的 Hasor Boot 归档。

```bash
mvn package
java -jar target/demo-hasor-boot-basic-@project.docsVersion@.jar
```

Web 应用也使用同样的 `java -jar` 方式运行：

```bash
java -jar target/demo-hasor-boot-web-@project.docsVersion@.jar
```

## Gradle 打包插件

插件 ID 为 `net.hasor.boot`。插件解析仓库与普通依赖仓库需要分别配置；下面也配置了 Maven Local，以便使用本地构建的产物。

```groovy title="settings.gradle"
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
rootProject.name = 'demo'
```

```groovy title="build.gradle"
plugins {
    id 'java'
    id 'net.hasor.boot' version '@project.docsVersion@'
}

version = '1.0.0'
repositories {
    mavenLocal()
    mavenCentral()
}
java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}
configurations {
    bootLoader
}
dependencies {
    implementation 'net.hasor:hasor-boot:@project.docsVersion@'
    bootLoader 'net.hasor:hasor-boot-loader:@project.docsVersion@'
}
tasks.named('bootJar') {
    mainClass.set('com.example.Application')
    loaderClasspath.from(configurations.bootLoader)
}
```

执行 `./gradlew bootJar` 后生成 `build/libs/demo-1.0.0-boot.jar`，使用 `java -jar build/libs/demo-1.0.0-boot.jar` 运行。`assemble` 也会依赖 `bootJar`；默认保留普通 jar，可执行包使用 `boot` classifier。`mainClass` 未设置时会读取普通 jar 的 Manifest `Main-Class`；`loaderClasspath` 必须显式配置。

## 构建 Hasor 源码

Hasor 仓库本身使用 Gradle Wrapper，以上 Maven 配置用于消费 Hasor 的应用工程。在仓库根目录执行：

```bash
./build.sh package test
./build.sh install test
```

`package` 构建产物；`install` 还会安装到 Maven Local，包括独立构建的 Gradle 插件。脚本只有带 `test` 参数才执行测试；直接使用 `./gradlew build` 则遵循 Gradle 的正常测试流程。

`deploy` 用于正式版本的 Maven Central 上传。它在实际构建前清理旧的 Central bundle 目录与 ZIP，防止旧产物混入；`--dry-run` 不执行清理和上传。发布参数见仓库 `build.sh --help`。
