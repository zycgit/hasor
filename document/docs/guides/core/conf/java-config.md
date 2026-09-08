---
id: java-config
sidebar_position: 6
title: Java 注解配置
description: 使用 hasor-config 声明 Bean、限定扫描范围并配置 Web MVC。
---

# Java 注解配置

`hasor-config` 在 `hasor-core` 上提供可选的注解配置，不要求应用实现 `Module`。使用同版本依赖：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-config</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

## 启动应用

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

`ApplicationBoot` 要求主类带有 `@Configuration` 且位于具名包。它把主类所在包设为 `hasor.config.scanPackages`，并注册为 primarySource。`create(Application.class)` 返回可继续设置参数的 `Hasor` 构建器；`run` 会绑定参数并创建 AppContext。主类及其子包中的 `@Configuration` 会被发现。

已有 Module 应用也可以显式安装配置：

```java
import net.hasor.config.core.ConfigurationModule;

apiBinder.installModule(ConfigurationModule.of(Application.class));
// 或限定包扫描：
apiBinder.installModule(ConfigurationModule.scan("com.example"));
```

## Bean 工厂与生命周期

`@Bean` 标注配置类自身声明的方法，以返回类型注册 Bean。方法参数按类型从容器获取，可用于表达 Bean 间依赖。

- 默认 `singleton = true`，注册为 eager singleton；`singleton = false` 使用 prototype。
- 未指定 `value` 时，方法名作为绑定 ID；显式 `@Bean("name")` 同时设置绑定 ID 和名称。
- 方法不能是 `static`、抽象方法或返回 `void`，实际返回值不能为 `null`。
- `initMethod` 在工厂创建对象后执行；`destroyMethod` 在 AppContext 关闭时对已创建并记录的对象执行。两者都需要公开的无参方法。
- 配置类不会被增强为拦截内部方法调用的代理；直接调用另一个 `@Bean` 方法仍是普通 Java 调用。需要容器中的对象时使用工厂方法参数注入。

配置类也可实现 `Module` 或 `WebMvcConfigurer`，参与模块配置与生命周期。模块配置阶段尚未完成字段注入，不应在 `loadModule` 等配置回调中依赖已注入的业务对象。

## 自动扫描配置

`hasor-config` 通过 `META-INF/hasor.schemas` 加载默认模块。配置项如下：

| 配置项 | 默认值 / 回退规则 | 环境变量 |
| --- | --- | --- |
| `hasor.config.autoScan` | `true`，控制配置类自动扫描及 Web 扫描扩展的安装 | `HASOR_CONFIG_AUTO_SCAN` |
| `hasor.config.scanPackages` | 未设置时回退到 `hasor.loadPackages` | `HASOR_CONFIG_SCAN_PACKAGES` |
| `hasor.config.web.autoScan` | `true`，控制 Controller 自动扫描 | `HASOR_CONFIG_WEB_AUTO_SCAN` |
| `hasor.config.web.scanPackages` | 依次回退到 `hasor.config.scanPackages`、`hasor.loadPackages` | `HASOR_CONFIG_WEB_SCAN_PACKAGES` |

应明确限定业务包，不能仅依赖框架默认扫描范围。`ApplicationBoot.create` 会设置配置扫描包，如需调整可继续调用 `addSettings(Settings.DefaultNameSpace, "hasor.config.scanPackages", "com.example")`。

配置类扫描在所有回退范围为空时直接返回；Web 扫描模块被安装后若仍无有效范围则抛出异常。关闭 `hasor.config.autoScan` 不会禁止显式安装 `ConfigurationModule`。

## Web Controller 与 MVC 配置

Web 能力需要额外引入 `hasor-web`（或传递依赖它的内嵌容器模块）；`hasor-config` 不会自动引入 Web 运行环境。必须以 `ServletContext` 创建 Hasor，内嵌服务示例见 [Web 启动器](../../deployment/web-launcher.md#自定义-appcontext-创建)。

Web 自动配置在限定范围内查找 `@MappingTo` 类并注册路由，配置阶段取得 Controller Provider，请求时再通过 Provider 获取实例。Controller 使用 Hasor 依赖注入，无需逐个调用 `loadMappingTo`。根路径 `@MappingTo("/")` 也受支持。

配置类可以实现 `WebMvcConfigurer`：

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
                .setWelcomeFile("home.html")
                .setOrder(-100);
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
        configurer.useDefaultJsonRenderEngine();
    }
}
```

## 注解 AOP

`hasor-config` 默认安装 `net.hasor.config.aop.AopModule`，支持 `net.hasor.cobble.dynamic.Aop` 注解。类级拦截器先于方法级拦截器执行。原有 `ApiBinder.bindInterceptor` 编程式 AOP 仍属于核心能力，详见 [类级拦截器](../aop/classlevel.md)。
