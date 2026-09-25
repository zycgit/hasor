---
id: exception
sidebar_position: 6
title: 4.4.6 异常处理
description: 按异常类型注册 ExceptionHandler，以及用配置工厂自动注册。
---

# 4.4.6 异常处理

本节功能自 **5.3.0** 起提供。

`hasor-web` 提供 `net.hasor.web.ExceptionHandler<E extends Throwable>`，接收当前 `Invoker` 和异常，返回要响应的数据。
通过 `WebApiBinder.addExceptionHandler(...)` 注册处理的异常类型：

```java
binder.addExceptionHandler(IllegalArgumentException.class, (invoker, error) -> {
    invoker.getHttpResponse().setStatus(400);
    return Map.of("message", "Invalid request");
});
binder.addExceptionHandler(RuntimeException.class, (invoker, error) -> {
    invoker.getHttpResponse().setStatus(500);
    return Map.of("message", "Request failed");
});
```

框架按实际抛出的异常类型向父类查找，选择最具体的注册类型，与注册顺序无关。
上例中的 `IllegalArgumentException` 进入第一个处理器。没有匹配的处理器时，原异常继续向外抛出。
同一种异常类型重复注册会在启动时失败，避免配置相互覆盖。
异常处理器的注册定义在 Web 上下文初始化时加载，请求时直接使用这些定义进行类型匹配。

- 返回非 `null`：该值成为最终调用结果，再按[返回值渲染规则](./defaults.md)和 `SkipRender` 状态决定是否输出。对象默认 JSON，字符串默认文本，`false`、`0`、空字符串也是有效结果；原 Action 返回 `void` 不影响这一规则。
- 返回 `null`：继续抛出原异常，不再尝试更宽泛的处理器；即使处理器已经写入响应或调用 `setSkipRender()`，也不改变此约定。
- 处理器自身抛出异常：向外抛出新异常，并将原异常保存为 suppressed exception，不递归处理。

状态码由处理器设置。处理器中的数据应当符合应用对外的错误响应约定。
自行写出响应的处理器仍需返回非空值（例如空字符串）表示异常已处理，框架不会追加正文；只返回状态码和响应头时，也可返回空字符串。
处理器可调用 `invoker.setSkipRender()` 跳过返回值渲染，但仍需返回非空值表示异常已处理。
`setSkipRender()` 只控制是否渲染，不跳过异常处理器，也不吞掉未处理的异常。
响应已经提交或业务已取得 Writer/输出流时，不再尝试异常响应；异常继续交给外层处理。
已匹配 MVC 路由中的 `preHandle`、Controller 和 `postHandle` 异常共用此机制，由 `InvokerCaller` 统一组织。
调用及异常处理结束后，再根据 `isSkipRender()` 决定是否渲染最终结果。渲染失败直接向外抛出，不再次进入异常处理器；渲染异常处理器返回的数据时若失败，原调用异常作为 suppressed exception 保留。
正常结束或异常已解决，且后续渲染成功或被跳过时，已进入的拦截器逆序执行 `afterCompletion(invoker, null)`。调用、异常处理器或渲染最终失败时，完成回调接收向外抛出的异常。
完成回调自身的错误只记录日志，不覆盖已有结果，也不再次进入异常处理器。
HTTP 过滤器位于 MVC 外层，过滤器自身、静态资源和后续 Servlet 的异常不进入此机制；外层过滤器可捕获 MVC 未解决的异常。
Controller 返回类型为 `void` 或标注 `@Async` 都不改变异常处理约定：没有匹配的处理器或处理器返回 `null` 时，异常继续向外抛出。
框架 `@Async` Action 在工作线程调用异常处理器。异步状态下的响应输出及未处理异常传播有单独限制，见[异步请求](../j2ee/async.md)；应用自行创建的异步任务由其异步处理机制负责。

## 通过配置工厂注册

引入 `hasor-config` 后，在 `@Configuration` 中声明 `@Exception` 工厂方法。
注解类型为 `net.hasor.config.web.Exception`，方法返回 `ExceptionHandler`，方法参数从容器注入：

```java
import java.io.IOException;
import java.util.Map;
import net.hasor.config.Configuration;
import net.hasor.config.web.Exception;
import net.hasor.web.ExceptionHandler;

@Configuration
public class ErrorConfiguration {
    @Exception({IOException.class, IllegalStateException.class})
    public ExceptionHandler<Throwable> requestErrors() {
        return (invoker, error) -> {
            invoker.getHttpResponse().setStatus(500);
            return Map.of("message", "Request failed");
        };
    }
}
```

配置类可由 `hasor.loadPackages` 自动扫描，也可用 `ConfigurationModule.of(ErrorConfiguration.class)` 显式加载。
工厂默认单例，Bean ID 使用方法名；多个异常类型复用同一实例，工厂参数和配置类字段使用既有依赖注入机制。
工厂不会在模块注册期间调用，其依赖由容器完成初始化后再使用。
单独使用 `@Exception` 即可注册工厂；与 `@Bean` 同时标注时复用已有 Bean 绑定，不会重复注册工厂。
可以叠加 `@Bean(value = "hostErrors", initMethod = "init", destroyMethod = "destroy")` 设置名称和生命周期，或用 `singleton = false` 改为 prototype，
也可声明 `@DependsOn`；处理器返回类型必须兼容注解列出的每一种异常。
工厂遵循 [Bean 方法约束](../../core/conf/java-config.md)：不能是静态或抽象方法，创建的处理器不能为 `null`。`@Exception` 至少声明一种异常类型，并且只能用于 Web 应用。
`@Exception` 自动注册与 `WebApiBinder` 手动注册共用同一个类型注册表和匹配规则。
