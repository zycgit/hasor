---
id: java-config
sidebar_position: 6
title: 2.8.5 Java 注解配置
description: 使用 hasor-config 声明 Bean、限定扫描范围并配置 Web MVC。
---

# 2.8.5 Java 注解配置

`hasor-config` 在 `hasor-core` 上提供可选的注解配置，不要求应用实现 `Module`。使用同版本依赖：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-config</artifactId>
    <version>@project.docsVersion@</version>
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

}

```

业务类独立放在 `Greeting.java`：

```java
package com.example;

public class Greeting {
    public String message() {
        return "Hello Hasor";
    }
}
```

`ApplicationBoot` 要求主类带有 `@Configuration`，将其注册为 primarySource，但不会推导扫描包。`create(...)` 返回 Hasor 构建器，`run(...)` 绑定参数并创建 AppContext。扫描范围统一由 `hasor.loadPackages` 指定。

已有 Module 应用也可以显式安装配置：

```java
import net.hasor.config.ConfigurationModule;

apiBinder.installModule(ConfigurationModule.of(Application.class));
// 或按 Core 配置的扫描范围装配：
apiBinder.installModule(ConfigurationModule.auto());
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

引入 Config 后，默认模块自动安装 `ConfigurationModule`，无需重复安装。
`auto()` 和公开无参构造方法按 Core 范围扫描；`of(...)` 只处理显式配置类，不顺带扫描路由。
子类可通过受保护的构造方法 `super(Application.class)` 指定配置类。

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.loadPackages>com.example.*</hasor.loadPackages>
</config>
```

多个包用逗号分隔；环境变量为 `HASOR_LOAD_PACKAGES`。启动类所在包不会自动成为扫描范围。
Config、Web、Boot 专用的 `scanPackages`、`autoScan` 配置及环境变量已不再读取。
空范围不扫描。Scanner 遍历一次类路径，先处理配置类和 Bean，再处理 Web 路由；没有 Web 依赖时跳过路由处理。

## Web Controller 与 MVC 配置

Web 能力需要额外引入 `hasor-web`（或传递依赖它的内嵌容器模块）；`hasor-config` 不会自动引入 Web 运行环境。必须以 `ServletContext` 创建 Hasor，内嵌服务示例见 [Web 启动器](../../deployment/web-launcher.md#自定义-appcontext-创建)。

Web 自动配置在限定范围内查找 `@MappingTo` 类并注册路由，配置阶段取得 Controller Provider，请求时再通过 Provider 获取实例。Controller 使用 Hasor 依赖注入，无需逐个调用 `loadMappingTo`。根路径 `@MappingTo("/")` 也受支持。

配置类可以实现 `WebMvcConfigurer`：

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

## 注解 AOP

`hasor-core` 默认安装 `net.hasor.core.aop.AopModule`，无需 Config 依赖，支持 `net.hasor.cobble.dynamic.Aop` 注解。类级拦截器先于方法级拦截器执行。原有 `ApiBinder.bindInterceptor` 编程式 AOP 仍属于核心能力，详见 [类级拦截器](../aop/classlevel.md)。


配置方法可以使用 `@DependsOn` 声明显式初始化依赖，详见[初始化依赖](../life/depends-on.md)。
