# Hasor Config

Hasor Config 是基于 Hasor Core 的声明式配置模块，通过 `@Configuration` 与 `@Bean` 组织对象创建和依赖装配。
复用 Core 的容器与扫描范围，将配置类、Bean 工厂和 Web 路由接入统一装配流程，减少手工注册。
既可独立用于普通 Java 应用，也可搭配 Hasor Web 定制 Web MVC，或由 Hasor Boot 集成启动。

## 特性

- 声明配置：通过 `@Configuration` 组织配置类，用 `@Bean` 方法声明容器管理的对象。
- 自动发现：统一使用 Core 的 `hasor.loadPackages` 扫描范围，也可显式指定配置类。
- 依赖装配：自动注入 Bean 工厂方法的参数，复用 Core 的依赖注入能力。
- 对象管理：支持 Bean 命名、单例与多实例，以及初始化和销毁方法。
- 模块复用：配置类可实现 Module，在同一处组合 Bean 声明与代码装配。
- 路由注册：搭配 Hasor Web 自动注册 `@MappingTo` 路由，配置类先于路由装配。
- Web 配置：通过 WebMvcConfigurer 配置静态资源、跨域规则和 JSON 渲染器。
- 独立使用：不依赖 Boot，非 Web 应用无需引入 Servlet 或 JSON 库。

## 快速开始

以下示例通过配置类创建两个 Bean，并自动注入工厂方法的依赖。

### 1. 引入依赖

在应用中引入 `net.hasor:hasor-config`，版本与项目使用的 Hasor 保持一致。
Web 应用还需引入 `net.hasor:hasor-web`；普通 Java 应用不需要。

### 2. 编写配置类

先定义独立的 `Message.java`：

```java
package example.config;

public class Message {
    private final String text;

    public Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
```

再通过 `AppConfig.java` 声明 Bean：

```java
package example.config;
import net.hasor.config.Bean;
import net.hasor.config.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public String greeting() {
        return "你好，Hasor Config！";
    }

    @Bean
    public Message message(String greeting) {
        return new Message(greeting);
    }

}
```

`message` 方法的 `greeting` 参数由容器提供，无需手动调用另一个 Bean 方法。
Bean 默认使用单例；可通过 `@Bean` 的属性指定名称、作用域方式和生命周期方法。

### 3. 加载配置并启动

```java
package example.config;
import net.hasor.config.ConfigurationModule;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;

public class Application {
    public static void main(String[] args) {
        try (AppContext context = Hasor.create()
                .build(ConfigurationModule.of(AppConfig.class))) {
            System.out.println(context.getInstance(Message.class).getText());
        }
    }
}
```

运行后输出 `你好，Hasor Config！`。`ConfigurationModule.of(...)` 只处理明确指定的配置类，不执行扫描。

### 4. 使用自动发现

需要自动发现时，在应用的 `hconfig.xml` 中声明扫描范围：

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.loadPackages>example.config.*</hasor.loadPackages>
</config>
```

将启动语句改为 `Hasor.create().mainSettingWith("hconfig.xml").build()`。
Config 已通过框架配置登记自动装配模块，无需再次手工注册；也可通过 `ConfigurationModule.auto()` 显式表达自动装配意图。
扫描范围统一由 Core 管理，不会根据应用入口类推断包名。

## 使用文档

[Hasor Config 使用文档](../document/docs/guides/core/conf/java-config.md)
