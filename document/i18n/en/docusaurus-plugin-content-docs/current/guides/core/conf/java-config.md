---
id: java-config
sidebar_position: 6
title: Java Configuration
description: Declare Beans, bound package scans, and customize Web MVC with hasor-config.
---

# Java Configuration

Add `net.hasor:hasor-config:5.0.2-SNAPSHOT` for optional annotation configuration on top of `hasor-core`.

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

    public static class Greeting {
        public String message() {
            return "Hello Hasor";
        }
    }
}
```

`ApplicationBoot` requires a `@Configuration` class in a named package. It sets that package as `hasor.config.scanPackages` and registers the class as a primarySource. `create` returns a customizable Hasor builder; `run` binds arguments and builds the context. Configuration classes in the package and its subpackages are discovered.

Existing Modules can explicitly install `ConfigurationModule.of(Application.class)` or `ConfigurationModule.scan("com.example")` from `net.hasor.config.core`.

## Bean Factories

`@Bean` registers methods declared on the configuration class by return type. Parameters are resolved by type from the container.

- The default is an eager singleton; `singleton = false` uses prototype scope.
- Without a value, the method name becomes the binding ID. `@Bean("name")` sets both ID and binding name.
- Methods cannot be static, abstract, or return void. Returned objects cannot be null.
- `initMethod` runs after creation. `destroyMethod` runs at context shutdown for recorded instances. Both must name public no-argument methods.
- Direct calls between Bean methods are ordinary Java calls, not intercepted container lookups. Use method parameters for managed dependencies.

Configuration classes can implement `Module` or `WebMvcConfigurer`. Configuration callbacks run before field injection is complete; do not rely on injected business objects during these callbacks.

## Package Scanning

Default modules are loaded through `META-INF/hasor.schemas`.

| Setting | Default / fallback | Environment variable |
| --- | --- | --- |
| `hasor.config.autoScan` | `true`; enables configuration scanning and Web scan extension installation | `HASOR_CONFIG_AUTO_SCAN` |
| `hasor.config.scanPackages` | Falls back to `hasor.loadPackages` | `HASOR_CONFIG_SCAN_PACKAGES` |
| `hasor.config.web.autoScan` | `true`; enables Controller scanning | `HASOR_CONFIG_WEB_AUTO_SCAN` |
| `hasor.config.web.scanPackages` | Falls back to config scan packages, then `hasor.loadPackages` | `HASOR_CONFIG_WEB_SCAN_PACKAGES` |

Set a bounded business package explicitly. `ApplicationBoot.create` sets the configuration scan package; override it with `addSettings(Settings.DefaultNameSpace, "hasor.config.scanPackages", "com.example")` when needed. Configuration scanning returns when all package sources are empty. An installed Web scan module throws if no valid range remains. Disabling automatic scanning does not prevent explicit `ConfigurationModule` installation.

## Web MVC

Add `hasor-web` or a container module and create Hasor with a ServletContext. `hasor-config` does not bring a Web runtime transitively. See the [Web factory example](../../deployment/web-launcher.md#custom-appcontext-factory).

Web scanning registers `@MappingTo` classes within the configured packages, capturing their Providers during configuration and resolving instances for requests. Controllers use Hasor injection. Root mapping `@MappingTo("/")` is supported.

```java
package com.example;

import net.hasor.config.Configuration;
import net.hasor.config.web.CorsRegistry;
import net.hasor.config.web.JsonRenderConfigurer;
import net.hasor.config.web.ResourceHandlerRegistry;
import net.hasor.config.web.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/web-assets/")
                .setWelcomeFile("home.html").setOrder(-100);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://hasor.net")
                .allowedMethods("GET", "POST")
                .allowedHeaders("content-type", "authorization")
                .allowCredentials(true).maxAge(1800);
    }

    @Override
    public void configureJson(JsonRenderConfigurer configurer) {
        configurer.useDefaultJsonRenderEngine();
    }
}
```

## Annotation AOP

`hasor-config` installs `net.hasor.config.aop.AopModule` for `net.hasor.cobble.dynamic.Aop`. Class interceptors run before method interceptors. Programmatic `ApiBinder.bindInterceptor` remains a core API.
