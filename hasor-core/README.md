# Hasor Core

Hasor Core 是轻量级 Java 依赖注入容器，提供对象装配、作用域管理、方法拦截和应用生命周期管理。
通过 Module 与 ApiBinder 组织模块，支持注解与代码配置，并提供统一的配置读取、事件通知和扩展接口。
既可独立用于普通 Java 应用，也作为 Hasor Config、Hasor Web 和 Hasor Boot 的基础容器。

## 特性

- 低侵入性：普通 Java 类即可交给容器管理，支持 Hasor 注解及 JSR-330 依赖注入。
- 模块装配：通过 Module 与 ApiBinder 声明绑定关系，按模块组织和复用应用配置。
- 依赖注入：支持构造器、字段和方法注入，以及按类型、名称或 ID 查找对象。
- 对象管理：支持单例、自定义作用域、预创建对象及 Provider 工厂。
- 方法拦截：通过注解或匹配规则配置 AOP，为方法调用增加公共处理逻辑。
- 生命周期：管理对象初始化与销毁，支持容器启动、关闭阶段的回调。
- 配置读取：统一读取 XML、YAML、Properties，支持环境变量引用。
- 事件扩展：支持同步、异步事件通知，通过 SPI 和自定义模块扩展容器能力。

## 快速开始

以下示例将服务交给容器管理，并通过字段注入使用它。

### 1. 引入依赖

在应用中引入 `net.hasor:hasor-core`，版本与项目使用的 Hasor 保持一致。
独立使用 Core 不需要 Web 框架或 Servlet 容器。

### 2. 编写服务

```java
package example.core;

public class GreetingService {
    public String hello() {
        return "你好，Hasor Core！";
    }
}
```

```java
package example.core;
import net.hasor.core.Inject;

public class GreetingClient {
    @Inject
    private GreetingService service;

    public void run() {
        System.out.println(service.hello());
    }
}
```

### 3. 注册并启动容器

```java
package example.core;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;

public class Application {
    public static void main(String[] args) {
        try (AppContext context = Hasor.create().build(binder -> {
            binder.bindType(GreetingService.class).asEagerSingleton();
            binder.bindType(GreetingClient.class);
        })) {
            context.getInstance(GreetingClient.class).run();
        }
    }
}
```

运行后输出 `你好，Hasor Core！`。退出代码块时关闭容器，执行对应的销毁和关闭回调。
绑定逻辑也可提取为独立的 `Module`；需要使用 `@Configuration` 和 `@Bean` 声明配置时，可搭配 Hasor Config。

## 使用文档

[Hasor Core 使用文档](../document/docs/guides/core/overview.md)
