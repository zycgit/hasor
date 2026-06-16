---
id: with-springboot
sidebar_position: 2
title: 基于 Spring Boot
description: dbVisitor ORM 工具和 Spring Boot 整合使用。
---
# 基于 Spring Boot

## 用法

在 Spring Boot 中只需要一个 `@EnableHasor` 注解即可在 Spring 中开启 Hasor 的支持。

```java
@EnableHasor
@SpringBootApplication
public class ExampleApp {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
```

然后新建一个 Hasor 的 Module 并将其用 Spring 管理起来，同时通过 @DimModule 注解标记声明它即可。

```java
@DimModule
@Component()
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        ...
    }
}
```

最后启动 Spring Boot 项目看到 HasorBoot 的欢迎信息就表示一切都 OK了。

## 共享 Spring 配置

Hasor 可以读取 Spring 加载的属性文件，例如：`application.properties` 文件。

如果要在 Hasor 的 Settings 中使用 Spring 属性，可以配置 `@EnableHasor(useProperties = true)`。

```java
Settings settings = appContext.getSettings();

assert "HelloWord".equals(settings.getString("msg")); // 若 useProperties = false，这里获取不到任何值
```

## @EnableHasor 注解

:::tip
EnableHasor 注解是 Spring Boot 启动 Hasor 的根本，下面是这个注解的属性说明。
:::

**scanPackages**
- 用来配置扫描 Module 的范围，一般情况下如果 Module 已经被 Spring 作为 Bean 托管之后就无需在配置扫描范围。scanPackages 的作用是，用来加载那些还未被 Spring 托管的 Module。 Hasor 在加载这些 Module 的时候会 new 它们。

**mainConfig**
- 虽然共享 Spring 的配置已经解决了大部分配置文件读取的问题，但有时候还是需要更高级的 hconfig.xml 配置文件。这个时候就可以通过这个属性来指定 Hasor 的 hconfig.xml。

**useProperties**
- 是否将 Spring Environment 中的属性信息导入 Hasor Settings。默认值为 false，表示不导入。

**startWith**
- 用来声明启动入口。如果配置的启动入口类已经在 Spring 中托管，那么就会通过 Spring 进行创建。否则就直接 new 出这个对象。

**customProperties**
- 可以设定一些特殊的属性 K/V 信息传递给 Hasor Settings。这些属性只会在 Hasor 中存在，不会污染 Spring。

## @EnableHasorWeb 注解

:::tip
Hasor-Web 是一款和 Spring 无关的 WebMVC 框架。它的功能与 SpringMVC 是等价的，都是针对 JavaWeb 开发，同时都具备 Restful 能力。

而 @EnableHasorWeb 注解的功效就是在 SpringWeb 环境中启用 Hasor-Web 能力。
:::

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-web</artifactId>
    <version>4.2.5</version>
</dependency>
```

这个注解有三个属性配置：

**path**
- Hasor-Web 的全局拦截器配置的拦截路径，默认值是： /*

**order**
- 生效顺序，默认值是： 0 （仅在 Filter、Interceptor 模式下有效）

**at**
- Hasor-web 在 Spring 中的工作模式，由 net.hasor.spring.boot.WorkAt 枚举定义。默认是：Filter
- Filter：过滤器模式，以 web filter 的方式进行集成。
- Controller：控制器模式，以 springwebmvc 的 Controller 方式进行集成。(4.2.2版本中加入，推荐使用)
