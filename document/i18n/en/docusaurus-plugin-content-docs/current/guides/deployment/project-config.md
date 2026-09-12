---
id: project-config
sidebar_position: 2
title: Project Configuration
description: Configure Hasor Boot dependencies and Maven or Gradle executable archive plugins.
---

# Project Configuration

Hasor Boot project configuration has two parts: runtime dependencies and Maven/Gradle packaging plugins. Ordinary applications need only the core dependency and packaging plugin; Web applications also select an embedded container module. The versions below match the current source snapshot. Install the corresponding artifacts locally first, or use versions available in your dependency repository.

## Ordinary application dependencies

Add `hasor-boot` for an ordinary application using the unified `Boot.run(...)` entry point:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot</artifactId>
    <version>5.1.1-SNAPSHOT</version>
</dependency>
```

Running `main` directly during development requires no additional runtime dependency. Configure `hasor-boot-maven-plugin` in Maven when packaging an executable Fat Jar.

## Web application dependencies

Web applications need `hasor-web` and one embedded container module:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>5.1.1-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>5.1.1-SNAPSHOT</version>
</dependency>
```

Container modules bring in Boot, Config, and Web transitively. Include only one container: zero or multiple SPI implementations cause errors. The application must supply a JSON library for default JSON rendering; see [JSON Rendering](../webmvc/response/json_render.md).

Choose one of the following embedded container modules as needed:

- `hasor-boot-web-tomcat`
- `hasor-boot-web-jetty`
- `hasor-boot-web-undertow`

## Maven packaging plugin

The Hasor Boot Maven plugin repackages the ordinary jar as an executable archive during the `package` phase.

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
            <version>5.1.1-SNAPSHOT</version>
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

Instead of writing `Main-Class` through `maven-jar-plugin`, you can configure the Hasor Boot plugin directly:

```xml
<configuration>
    <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
</configuration>
```

## Building and running

Maven package generates a runnable Hasor Boot archive in the target directory.

```bash
mvn package
java -jar target/demo-hasor-boot-basic-5.1.1-SNAPSHOT.jar
```

Web applications run with the same `java -jar` command:

```bash
java -jar target/demo-hasor-boot-web-5.1.1-SNAPSHOT.jar
```

## Gradle packaging plugin

The plugin ID is `net.hasor.boot`. This example uses a source snapshot installed in Maven Local. Configure plugin resolution repositories separately from ordinary dependency repositories.

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
    id 'net.hasor.boot' version '5.1.1-SNAPSHOT'
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
    implementation 'net.hasor:hasor-boot:5.1.1-SNAPSHOT'
    bootLoader 'net.hasor:hasor-boot-loader:5.1.1-SNAPSHOT'
}
tasks.named('bootJar') {
    mainClass.set('com.example.Application')
    loaderClasspath.from(configurations.bootLoader)
}
```

Running `./gradlew bootJar` produces `build/libs/demo-1.0.0-boot.jar`; run it with `java -jar build/libs/demo-1.0.0-boot.jar`. `assemble` also depends on `bootJar`. The ordinary jar is retained by default; the executable archive uses the `boot` classifier. If `mainClass` is unset, the ordinary jar Manifest `Main-Class` is used. Configure `loaderClasspath` explicitly.

## Building Hasor from source

The Hasor repository itself uses Gradle Wrapper; the Maven configuration above is for applications consuming Hasor. From the repository root, run:

```bash
./build.sh package test
./build.sh install test
```

`package` builds artifacts; `install` also installs them in Maven Local, including the separately built Gradle plugin. The script runs tests only when the `test` argument is present. Running `./gradlew build` directly follows the normal Gradle test workflow.

`deploy` uploads official versions to Maven Central and rejects SNAPSHOT versions. Before building, it removes old Central bundle directories and ZIPs to prevent stale artifacts from being included. `--dry-run` does not clean or upload. See repository `build.sh --help` for publishing options.
