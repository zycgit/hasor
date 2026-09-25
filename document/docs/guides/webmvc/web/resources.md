---
id: resources
sidebar_position: 2
title: 4.1.1 静态资源与前端应用
description: 使用 ResourceBinder 配置资源来源和前端路由回退。
---

# 4.1.1 静态资源与前端应用

资源处理是 Hasor Web 的内置能力，不需要 Config 或 Boot。

```java
binder.addResource("/app/**", resourceLoader)
      .welcomeFile("index.html")
      .fallbackPaths("/app/tasks/*")
      .excludedPrefixes("/app/api")
      .cacheControl("no-cache");
```

返回 `net.hasor.web.binder.ResourceBinder` 配置访问策略，至少传入一个 Cobble ResourceLoader。
同一地址可组合多个来源：`binder.addResource("/assets/**", localLoader, classpathLoader)` 按顺序查找。
应使用这一方式实现来源回退，而不是重复注册同一映射。

映射采用前缀，可用 `/*` 或 `/**` 结尾。`order` 越小越优先；同序号时长前缀优先，再按声明顺序。
欢迎文件默认 index.html，不限制扩展名，null 可禁用。SPA 回退必须显式设置，不掩盖带扩展名的资源缺失。
支持 GET、HEAD、Last-Modified/304 校验，默认缓存策略为 no-cache。

## 与 Action、过滤器的关系

Action 始终优先，未匹配 Action 才尝试资源。资源规则命中但文件缺失返回 404，不继续其他规则；没有规则匹配才继续 Servlet 链。
资源不进入通过 `WebApiBinder.filter(...)` 或 `jeeFilter(...)` 注册的过滤器链，也不进入 MVC `HandlerInterceptor`、MVC 异常处理和返回值渲染。
需要对资源进行鉴权、审计或 CORS 处理时，应在宿主 Servlet 容器中注册 Filter。

## 配套装配

`WebMvcConfigurer.addResourceHandlers(WebApiBinder)` 使用同一 API。
Boot 默认装配 META-INF/resources（包括依赖 JAR 中的资源），SPA 回退默认关闭。
CORS 是 Config 的可选能力，过滤器位于 `net.hasor.config.web.cors.CorsFilter`；引入 Web 不会自动启用跨域策略。
旧 Registration、Registry、addResourceHandler 接口改为 addResource 和 ResourceBinder。
