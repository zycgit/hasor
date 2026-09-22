---
id: java-config
sidebar_position: 6
title: 2.8.5 Java Annotation Configuration
description: Declare Beans, restrict scanning, and configure Web MVC with hasor-config.
---

# 2.8.5 Java Annotation Configuration

`hasor-config` provides optional annotation configuration on top of `hasor-core`; applications do not have to implement `Module`. Use the matching dependency version:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-config</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

## Starting an application

```java
package com.example;

import net.hasor.config.ApplicationBoot;
import net.hasor.config.Bean;
import net.hasor.config.Configuration;

@Configuration
public class Application {
    public static void main(String[] args) {
        ApplicationBoot.run(Application.class, args);
    }

    @Bean
    public Greeting greeting() {
        return new Greeting();
    }

}

```

Place the business class in a separate `Greeting.java` file:

```java
package com.example;

public class Greeting {
    public String message() {
        return "Hello Hasor";
    }
}
```

`ApplicationBoot` requires the primary class to have `@Configuration` and registers it as a primarySource, but does not infer scan packages. `create(...)` returns a Hasor builder; `run(...)` binds arguments and creates AppContext. The scan scope is configured exclusively through `hasor.loadPackages`.

Existing Module-based applications can also install configuration explicitly:

```java
import net.hasor.config.ConfigurationModule;

apiBinder.installModule(ConfigurationModule.of(Application.class));
// Or assemble using the scan scope configured in Core:
apiBinder.installModule(ConfigurationModule.auto());
```

## Bean factories and lifecycle

Annotate methods declared by the configuration class with `@Bean` to register Beans by return type. Method parameters are resolved from the container by type and can express dependencies between Beans.

- By default, `singleton = true` registers an eager singleton; `singleton = false` uses prototype scope.
- If `value` is omitted, the method name becomes the binding ID; explicit `@Bean("name")` sets both the binding ID and name.
- Methods cannot be `static`, abstract, or return `void`; the actual return value must not be `null`.
- `initMethod` runs after the factory creates an object. `destroyMethod` runs on created and tracked objects when AppContext closes. Both must be public no-argument methods.
- Configuration classes are not enhanced with proxies that intercept internal method calls. Calling another `@Bean` method directly is still an ordinary Java call. Use factory method parameter injection when you need the container-managed object.

Configuration classes can also implement `Module` or `WebMvcConfigurer` to participate in module configuration and lifecycle. Field injection is not complete during module configuration; do not depend on injected business objects in callbacks such as `loadModule`.

## Automatic scanning

Adding Config automatically installs `ConfigurationModule` through the default module; no duplicate installation is needed.
`auto()` and the public no-argument constructor scan the Core scope. `of(...)` handles only explicitly supplied configuration classes and does not also scan routes.
Subclasses can specify configuration classes through the protected constructor `super(Application.class)`.

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.loadPackages>com.example.*</hasor.loadPackages>
</config>
```

Separate multiple packages with commas; the environment variable is `HASOR_LOAD_PACKAGES`. The startup class package is not automatically included in the scan scope.
The Config-, Web-, and Boot-specific `scanPackages` and `autoScan` settings and environment variables are no longer read.
An empty scope performs no scanning. Scanner traverses the classpath once, processing configuration classes and Beans before Web routes; route processing is skipped when Web dependencies are absent.

## Web controllers and MVC configuration

Web features require `hasor-web` or an embedded container module that depends on it transitively. `hasor-config` does not add a Web runtime automatically. Hasor must be created with a `ServletContext`; see [Web Launcher](../../deployment/web-launcher.md#custom-appcontext-creation) for an embedded server example.

Web auto-configuration finds `@MappingTo` classes within the configured scope and registers routes. It obtains Controller Providers during configuration and resolves instances through them at request time. Controllers use Hasor dependency injection without individual `loadMappingTo` calls. Root mappings such as `@MappingTo("/")` are also supported.

A configuration class can implement `WebMvcConfigurer`:

```java
package com.example;

import net.hasor.config.Configuration;
import net.hasor.config.web.cors.CorsRegistry;
import net.hasor.config.web.render.JsonRenderConfigurer;
import net.hasor.web.WebApiBinder;
import net.hasor.cobble.loader.providers.PrefixResourceLoader;
import net.hasor.config.web.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(WebApiBinder binder) {
        binder.addResource("/assets/**", new PrefixResourceLoader(binder.getResourceLoader(), "web-assets"))
                .welcomeFile("home.html")
                .order(-100);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://hasor.net")
                .allowedMethods("GET", "POST")
                .allowedHeaders("content-type", "authorization")
                .allowCredentials(true)
                .maxAge(1800);
    }

    @Override
    public void configureJson(JsonRenderConfigurer configurer) {
        configurer.renderEngine(net.hasor.web.render.json.JsonRenderEngine.class);
    }
}
```

## Annotation-based AOP

`hasor-core` installs `net.hasor.core.aop.AopModule` by default, supporting `net.hasor.cobble.dynamic.Aop` without a Config dependency. Class-level interceptors run before method-level interceptors. Programmatic AOP through `ApiBinder.bindInterceptor` remains a Core feature; see [Class-Level Interceptors](../aop/classlevel.md).


Factory methods can declare explicit initialization dependencies with `@DependsOn`; see [initialization dependencies](../life/depends-on.md).
