---
id: depends-on
sidebar_position: 6
title: 2.6.5 显式初始化依赖
description: 使用 DependsOn 声明 Hasor Bean 的初始化依赖。
---

# 2.6.5 显式初始化依赖

`dependsOn(...)` 声明当前 Bean 创建前必须取得的容器依赖。依赖完成同步初始化后，才创建当前对象并执行其注入、初始化。依赖在实际创建时解析，所以可以由后续模块注册，不需要调整扫描顺序。

```java
binder.bindType(Storage.class).idWith("storage");
binder.bindType(Service.class).dependsOn("storage");
```

绑定 API 支持四种形式：

| 调用 | 选择规则 |
| --- | --- |
| `dependsOn("storage")` | 精确绑定 ID，不是仅有名称的绑定 |
| `dependsOn(Storage.class)` | 已注册类型，必须恰好匹配一个绑定 |
| `dependsOn("primary", Storage.class)` | 名称和已注册类型同时匹配 |
| `dependsOn(storageBinding)` | 使用已有 `BindInfo` 的绑定 ID |

多次调用会合并依赖。字符串 ID、类型和绑定引用形式支持多个参数。同类型存在多个绑定时，应指定名称或绑定引用，不能依赖注册顺序。

## 类型与配置方法注解

```java
@DependsOn(value = "storage", types = AuditService.class)
public class Service {
    // Dependencies are ready before the constructor runs.
}
```

`net.hasor.core.DependsOn` 支持类型和配置方法。类型注解可继承，配置方法由 `hasor-config` 转换为同一套核心绑定声明：

```java
@Configuration
public class ApplicationConfiguration {
    @Bean(value = "storage", initMethod = "init")
    public Storage storage() {
        return new Storage();
    }

    @Bean
    @DependsOn("storage")
    public Service service() {
        return new Service();
    }
}
```

配置方法未指定 `@Bean` 值时，方法名就是绑定 ID。指定值时，该值同时作为绑定 ID 和名称。

## 创建与生命周期

普通对象在构造之前解析依赖；自定义 Provider 在被调用之前解析依赖。依赖初始化失败时，当前对象不会开始创建。这个声明不替自定义 Provider 执行其返回对象的注入或初始化，Provider 仍负责自己的对象生命周期；`@Bean(initMethod = ...)` 则由配置模块执行。

正常单例只在首次创建时取得依赖，原型每次创建时取得依赖。声明不会让原本按需创建的 Bean 全部提前实例化。原型依赖每次获取都可以产生新实例，`DependsOn` 不负责将该实例注入当前 Bean。

依赖缺失、同类型存在多个候选、直接或间接循环都会在解析时失败。显式依赖与构造参数、属性注入共享核心创建链的循环检测，异常包含依赖路径；检测不依赖全局拓扑排序。

`binder.getProvider(name, type)` 使用相同的名称和类型选择规则；名称为空时要求类型唯一。绑定阶段只取得 Supplier，容器初始化通知完成后才能调用它。已有按类型、按绑定引用的 Provider 也在这一阶段获得上下文。

本功能保证同步创建和初始化的先后关系，不新增异步就绪等待或依赖反向销毁规则。
