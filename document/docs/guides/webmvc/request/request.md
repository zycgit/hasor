---
id: request
sidebar_position: 1
title: 4.2 接收 Web 请求
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.2 接收 Web 请求

接收Web请求，下面是最简形态：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute() {
        ...
    }
}
```

`@MappingTo` 负责声明请求路径。上面的类级映射中，方法需要使用 `@Any`、`@Get`、`@Post` 等 HTTP 方法注解；`@Any` 表示接收任意 HTTP 方法。

也可以将 `@MappingTo` 直接标注在具体方法上，由框架分别注册路径：

```java
public class UserController {
    @Get
    @MappingTo("/users/{id}")
    public String find(@PathParameter("id") String id) {
        return id;
    }

    @Post
    @MappingTo("/users")
    public String create() {
        return "created";
    }
}
```

- 方法必须是 `public` 实例方法。方法级 `@MappingTo` 没有搭配 HTTP 方法注解时，默认接收任意 HTTP 方法。
- 同时在类和方法上声明 `@MappingTo` 时，类路径作为公共前缀，例如类上的 `/users` 与方法上的 `/{id}` 组合为 `/users/{id}`。此时只有标注了方法级映射的方法会成为接口。
- 支持多个路径和重复 `@MappingTo`。同一路径可以由不同 HTTP 方法处理；重复的路径与 HTTP 方法组合在注册时抛出异常。
- `hasor-config` 自动扫描也支持只有方法级映射的控制器，无需增加类级路由或自行分发请求。

映射路径必须以 `/` 开头，也允许 `@MappingTo("/")` 接收应用根路径请求。使用 `WebApiBinder.loadMappingTo` 显式注册，或引入 [hasor-config](../../core/conf/java-config.md) 并配置 Controller 扫描范围。
