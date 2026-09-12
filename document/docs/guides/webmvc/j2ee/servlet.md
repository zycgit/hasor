---
id: servlet
sidebar_position: 1
title: Servlet
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# Servlet

使用 Servlet 如下所示：

```java
@Singleton
@MappingTo("/your_point.do")
public class DemoHttpServlet extends HttpServlet {
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        ...
    }
}
```

然后注册 Servlet

```java
public class DemoModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        // 扫描所有带有 @MappingTo 注解类
        Set<Class<?>> aClass = apiBinder.findClass(MappingTo.class, "com.example.web.servlet.*");
        // 对 aClass 集合进行发现并自动配置 Servlet
        apiBinder.loadMappingTo(aClass);
    }
}
```
