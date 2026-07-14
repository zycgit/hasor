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
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

If you only run the `main` method during development, no extra runtime dependency is required. To package an executable fat jar, configure `hasor-boot-maven-plugin` in Maven.

## Web Application Dependencies

Web applications need `hasor-web` and one embedded container module:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-boot-tomcat</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

The embedded container module can be replaced as needed:

- `hasor-boot-tomcat`
- `hasor-boot-jetty`
- `hasor-boot-undertow`

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
java -jar target/demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar
```

Web applications use the same `java -jar` form:

```bash
java -jar target/demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```
