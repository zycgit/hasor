---
id: project-config
sidebar_position: 2
title: Project Configuration
description: Configure Hasor Boot dependencies, embedded container dependencies, and the Maven executable package plugin.
---

# Project Configuration

Hasor Boot project configuration has two parts: runtime dependencies and the Maven packaging plugin. Ordinary applications only need the core dependency and packaging plugin. Web applications also choose one embedded container module.

## Ordinary Application Dependencies

Ordinary Java applications need at least `hasor-core`:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-core</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

If you only run the `main` method during development, no extra runtime dependency is required. To package an executable fat jar, configure `hasor-boot-maven-plugin` in Maven.

## Web Application Dependencies

Web applications need `hasor-web` and one embedded container module:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-web-tomcat</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

The embedded container module can be replaced as needed:

- `hasor-boot-web-tomcat`
- `hasor-boot-web-jetty`
- `hasor-boot-web-undertow`

## Maven Packaging Plugin

The Hasor Boot Maven plugin repackages a regular jar into an executable archive during the `package` phase.

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
            <version>5.0.2-SNAPSHOT</version>
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

If you do not want to write `Main-Class` through `maven-jar-plugin`, configure the Hasor Boot plugin parameter directly:

```xml
<configuration>
    <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
</configuration>
```

## Build and Run

After Maven package, the target directory contains a Hasor Boot archive that can run directly.

```bash
mvn package
java -jar target/demo-hasor-boot-basic-5.0.2-SNAPSHOT.jar
```

Web applications use the same `java -jar` form:

```bash
java -jar target/demo-hasor-boot-web-5.0.2-SNAPSHOT.jar
```

## Gradle Packaging

The plugin ID is `net.hasor.boot`. Install the source snapshot to Maven Local first, or use versions available in your configured repositories. Plugin repositories and dependency repositories are configured separately:

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
    id 'net.hasor.boot' version '5.0.2-SNAPSHOT'
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
    implementation 'net.hasor:hasor-core:5.0.2-SNAPSHOT'
    bootLoader 'net.hasor:hasor-boot-loader:5.0.2-SNAPSHOT'
}
tasks.named('bootJar') {
    mainClass.set('com.example.Application')
    loaderClasspath.from(configurations.bootLoader)
}
```

Run `./gradlew bootJar`, then `java -jar build/libs/demo-1.0.0-boot.jar`. `assemble` also depends on `bootJar`. The default classifier is `boot`; the ordinary jar is retained. If `mainClass` is absent, the task reads the source jar Manifest. `loaderClasspath` must be configured explicitly.

## Building Hasor Sources

The Hasor repository uses Gradle Wrapper; Maven examples above are for consuming applications. From the repository root, run `./build.sh package test` to build, or `./build.sh install test` to also publish locally, including the separately built Gradle plugin. The script skips tests unless `test` is present; direct `./gradlew build` follows the normal Gradle test lifecycle.

`deploy` uploads release versions to Maven Central and rejects SNAPSHOT versions. Before the real build it clears the previous Central bundle directory and ZIP; `--dry-run` skips cleanup and upload. See `build.sh --help` for publishing parameters.
