---
id: hasor-boot
sidebar_position: 5
title: Hasor Boot Executable Packages
description: Use Hasor Boot to package executable fat jars and start Hasor applications with Hasor.run.
---

# Hasor Boot Executable Packages

Hasor Boot packages a regular Hasor application as a directly executable fat jar. It is responsible for two things:

- During build time, it participates in Maven packaging and reorganizes application classes and runtime dependencies into an executable archive.
- During runtime, Hasor Boot Loader creates the application `ClassLoader` and loads classes and resources from nested jars.

## Startup Entry

Hasor Boot recommends `Hasor.run(args, PrimarySource.class)` as the startup entry.

```java
public class DemoHasorBootApplication implements Module {
    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class).toInstance(new HelloService("Hasor Boot"));
    }
}
```

`Hasor.run` is equivalent to the following builder usage:

```java
Hasor.create()
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();
```

## primarySources

`primarySources` are the main startup sources for Hasor Boot. They differ from ordinary modules in the following ways:

- A primary source is always registered as a Hasor bean.
- It is created by Hasor, so the type must provide an accessible no-argument constructor. If other constructors are defined, keep a no-argument constructor as well.
- It is created as a singleton, and the `AppContext` obtains it before full dependency injection is performed.
- If it implements `Module`, the same instance also participates in the `loadModule`, `onStart`, and `onStop` lifecycle.
- When `loadModule` is called, the container is still in the module-configuration phase. Dependency injection has not yet been performed on the primary source, so do not use `@Inject` fields or injected values in `loadModule`.
- For the primary source itself, dependency injection happens after `loadModule` and before `onStart`.

A startup class can therefore handle module configuration and lifecycle logic at the same time:

```java
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.core.Module;
import net.hasor.core.info.Arguments;

public class DemoHasorBootApplication implements Module {
    private static final Logger logger = LoggerFactory.getLogger(DemoHasorBootApplication.class);

    @Inject
    private Arguments arguments;
    @Inject
    private HelloService helloService;

    public DemoHasorBootApplication() {
    }

    public static void main(String[] args) {
        Hasor.run(args, DemoHasorBootApplication.class);
    }

    @Override
    public void loadModule(ApiBinder apiBinder) {
        apiBinder.bindType(HelloService.class).toInstance(new HelloService("Hasor Boot"));
    }

    @Override
    public void onStart(AppContext appContext) {
        logger.info(this.helloService.sayHello(this.arguments));
    }

    @Override
    public void onStop(AppContext appContext) {
        logger.info(this.helloService.sayGoodbye(this.arguments));
    }
}
```

## Startup Arguments

`Hasor.run` binds the `main` method arguments into the container:

- `net.hasor.core.info.Arguments`
- A named `String[]` whose name is `Arguments.MAIN_ARGS`

Business beans or the primary source can inject `Arguments` directly:

```java
public class HelloService {
    public String sayHello(Arguments args) {
        return "hello Hasor Boot args=" + args;
    }
}
```

## Web Startup

Hasor Web applications can start an embedded container with `WebServers.run(args, RootModule.class)`. The application entry does not need to create `TomcatWebServer`, `JettyWebServer`, or `UndertowWebServer` manually. `WebServers` reads startup parameters from `hasor.http` configuration and discovers available container implementations on the classpath through Java SPI.

```java
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.web.http.WebServers;

public class DemoHasorBootWebApplication implements WebModule {
    public static void main(String[] args) throws Exception {
        WebServers.run(args, DemoHasorBootWebApplication.class).join();
    }

    @Override
    public void loadModule(WebApiBinder apiBinder) {
        apiBinder.loadMappingTo(HelloWebAction.class);
    }
}
```

`hasor-web` already provides a default `hasor.http` section in `web-hconfig.xml`. Applications only need to override a few runtime parameters in their own `hconfig.xml`. The Hasor Web `RuntimeFilter` name, match path, boot entry configuration file, and ServletContext static resource root are fixed internally by the framework and do not need to be exposed as HTTP configuration items. `hasor.layout.layoutPath` and `hasor.layout.templatePath` are only used for template lookup during rendering; they are not used as the static resource root for the embedded container.

```xml
<config>
    <hasor>
        <http>
            <host>0.0.0.0</host>
            <port>8080</port>
            <contextPath>/</contextPath>
        </http>
    </hasor>
</config>
```

`server` can explicitly select a container by name. If it is empty, the classpath is scanned through Java SPI for a `WebServerProvider`. The official container names are `tomcat`, `jetty`, and `undertow`.

```xml
<server>tomcat</server>
```

## Shutdown Hook

By default, Hasor registers a JVM shutdown hook. When the process exits normally or receives `SIGTERM` or `SIGINT`, Hasor calls `AppContext.shutdown()` and executes `Module#onStop`.

```java
Hasor.run(args, DemoHasorBootApplication.class);
```

If an application needs to control shutdown by itself, disable automatic registration:

```java
AppContext appContext = Hasor.create()
        .registerShutdownHook(false)
        .bindArguments(args)
        .addPrimarySources(DemoHasorBootApplication.class)
        .build();

appContext.shutdown();
```

:::tip
`kill -0 <pid>` only checks whether the process exists and whether the current user has permission to signal it. It does not notify the process to exit. Common exit notifications are `kill <pid>` and `kill -15 <pid>`, both of which trigger the JVM shutdown hook.
:::

## Maven Packaging

The Hasor Boot Maven plugin repackages a regular jar into a Hasor Boot executable archive during the `package` phase.

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

If you do not want to write `Main-Class` through `maven-jar-plugin`, configure the plugin parameter directly:

```xml
<configuration>
    <mainClass>net.hasor.demo.boot.DemoHasorBootApplication</mainClass>
</configuration>
```

Build and run:

```bash
mvn -pl demo-hasor-boot/demo-hasor-boot-basic -am package
java -jar demo-hasor-boot/demo-hasor-boot-basic/target/demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar demo
```

## Archive Layout

Hasor Boot executable archives mainly use the following layout. `APP-INF/hasor/` is a reserved configuration directory.

```text
META-INF/MANIFEST.MF
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

The `Main-Class` in the manifest points to Hasor Boot Loader, and the real application entry is written to `Hasor-Main-Class`:

```text
Main-Class: net.hasor.boot.loader.JarLauncher
Hasor-Main-Class: net.hasor.demo.boot.DemoHasorBootApplication
```

At runtime, `JarLauncher` creates the application `ClassLoader`:

- `APP-INF/classes/` is used as the application classpath.
- `APP-INF/lib/*.jar` is used as nested dependency jars.
- The loader reads classes, resources, and `META-INF/hasor.schemas` from nested jars.

The application can then be started with one command:

```bash
java -jar demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar
```
