---
id: with-springboot
sidebar_position: 2
title: Spring Boot Integration
description: Integrate Hasor with Spring Boot.
---
# Spring Boot Integration

## Usage

In Spring Boot, a single `@EnableHasor` annotation enables Hasor support inside Spring.

```java
@EnableHasor
@SpringBootApplication
public class ExampleApp {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
```

Then create a Hasor `Module`, let Spring manage it, and mark it with `@DimModule`.

```java
@DimModule
@Component()
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        ...
    }
}
```

After starting the Spring Boot application, seeing the Hasor Boot welcome message means the integration is working.

## Sharing Spring Configuration

Hasor can read property files loaded by Spring, such as `application.properties`.

To use Spring properties in Hasor `Settings`, configure `@EnableHasor(useProperties = true)`.

```java
Settings settings = appContext.getSettings();

assert "HelloWord".equals(settings.getString("msg")); // No value is available here when useProperties = false.
```

## The @EnableHasor Annotation

:::tip
`@EnableHasor` is the core annotation for starting Hasor in Spring Boot. Its attributes are described below.
:::

**scanPackages**
- Configures the package range used to scan for modules. Usually, if a module is already managed by Spring as a bean, scan configuration is unnecessary. `scanPackages` is used to load modules that are not yet managed by Spring; Hasor creates those modules directly.

**mainConfig**
- Sharing Spring configuration solves most configuration-loading needs, but some applications still need the more advanced `hconfig.xml` format. This attribute points Hasor to that `hconfig.xml` file.

**useProperties**
- Controls whether properties from the Spring `Environment` are imported into Hasor `Settings`. The default is `false`, meaning they are not imported.

**startWith**
- Declares the startup entry module. If the configured startup class is already managed by Spring, Spring creates it. Otherwise, Hasor instantiates it directly.

**customProperties**
- Provides special key-value properties to Hasor `Settings`. These properties exist only in Hasor and do not pollute Spring.

## The @EnableHasorWeb Annotation

:::tip
Hasor Web is a Web MVC framework independent of Spring. Its role is equivalent to Spring MVC: both target Java Web development and provide RESTful capabilities.

`@EnableHasorWeb` enables Hasor Web inside a Spring Web environment.
:::

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>4.2.5</version>
</dependency>
```

This annotation has three configurable attributes:

**path**
- The path pattern for the global Hasor Web interceptor. The default is `/*`.

**order**
- The effective order. The default is `0`. This only applies in Filter and Interceptor modes.

**at**
- The Hasor Web operating mode inside Spring, defined by the `net.hasor.spring.boot.WorkAt` enum. The default is `Filter`.
- `Filter`: filter mode, integrated as a web filter.
- `Controller`: controller mode, integrated as a Spring Web MVC controller. This mode was added in version 4.2.2 and is recommended.
