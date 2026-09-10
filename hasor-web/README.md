# Hasor Web

Hasor Web 是基于 Hasor 依赖注入容器的轻量级 Java Web MVC 框架，支持注解式路由、自动返回值渲染和静态资源托管。
框架提供开箱即用的默认行为，通过统一配置覆盖默认规则，并开放过滤器、渲染引擎和资源加载器等扩展接口。
既可独立集成到 Servlet 应用，也可搭配 Hasor Config、Hasor Boot 使用约定化装配与内嵌容器。

## 特性

- 低侵入性：普通 Java 类即可处理请求，无需继承框架基类，支持注解路由与依赖注入。
- J2EE 兼容：支持原生 Servlet、Filter 集成及 Servlet 异步请求。
- 参数绑定：通过注解读取请求参数，支持类型转换和对象分组。
- 请求验证：按业务场景组合和复用验证规则，统一收集字段错误。
- 文件上传：支持流式处理、内存与磁盘缓存，以及请求和文件大小限制。
- 资源托管：支持多个资源来源、缓存控制和单页应用路由回退。
- 响应渲染：内置 JSON、文本输出，可接入自定义渲染器和模板引擎。
- 页面布局：通过布局模板复用页头、导航和页脚，统一页面结构。

## 快速开始

以下示例使用 Servlet 容器部署，访问 `GET /hello` 时返回普通文本。

### 1. 引入依赖

在应用中引入 `net.hasor:hasor-web`，版本与项目使用的 Hasor 保持一致。
当前源码使用 `javax.servlet` 接口，需要选择兼容的 Servlet 容器。

默认的对象渲染使用 JSON，因此还需在运行时提供 Jackson 2、Gson、Fastjson 1 或 Fastjson 2 中的一种。

### 2. 编写请求处理类

```java
package example.web;
import net.hasor.web.annotation.Get;

public class HelloAction {
    @Get
    public String hello() {
        return "你好，Hasor Web！";
    }
}
```

### 3. 注册接口

```java
package example.web;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

public class StartModule implements WebModule {
    @Override
    public void loadModule(WebApiBinder binder) throws Throwable {
        binder.mappingTo("/hello").with(HelloAction.class);
        binder.setEncodingCharacter("UTF-8", "UTF-8");
    }
}
```

也可在类上使用 `@MappingTo("/hello")`，然后通过 `binder.loadMappingTo(HelloAction.class)` 注册。
独立使用 Web 时需要注册映射；使用 Config 或 Boot 的自动扫描时，按对应模块的扫描配置装配。

### 4. 配置 Servlet 入口

在 `web.xml` 中声明启动监听器、请求过滤器和启动模块：

```xml
<!-- 框架启动 -->
<listener>
    <listener-class>net.hasor.web.startup.RuntimeListener</listener-class>
</listener>
<!-- 全局拦截器 -->
<filter>
    <filter-name>rootFilter</filter-name>
    <filter-class>net.hasor.web.startup.RuntimeFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>rootFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
<!-- 建议指定启动模块 -->
<context-param>
    <param-name>hasor-root-module</param-name>
    <param-value>example.web.StartModule</param-value>
</context-param>
<!-- 可选：指定应用配置文件 -->
<context-param>
    <param-name>hasor-hconfig-name</param-name>
    <param-value>hconfig.xml</param-value>
</context-param>
```

将应用部署到 Servlet 容器后，访问应用上下文路径下的 `/hello`，
即可收到 UTF-8 编码的文本响应。

## 使用文档

[Hasor Web 使用文档](../document/docs/guides/webmvc/overview.md)
