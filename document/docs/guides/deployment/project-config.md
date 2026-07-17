---
id: project-config
sidebar_position: 2
title: 工程配置
description: 配置 Hasor Boot 依赖、内嵌容器依赖和 Maven 可执行包插件。
---

# 工程配置

Hasor Boot 的工程配置分为两部分：运行期依赖和 Maven 打包插件。普通应用只需要核心依赖和打包插件；Web 应用还需要选择一个内嵌容器模块。

## 普通应用依赖

普通 Java 应用至少引入 `hasor-core`：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-core</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

如果只是在开发阶段直接运行 `main` 方法，不需要额外运行期依赖。需要打包成可执行 Fat Jar 时，在 Maven 中配置 `hasor-boot-maven-plugin`。

## Web 应用依赖

Web 应用需要 `hasor-web`，并选择一个内嵌容器模块：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

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
            <version>${project.version}</version>
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
java -jar target/demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar
```

Web 应用也使用同样的 `java -jar` 方式运行：

```bash
java -jar target/demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```
