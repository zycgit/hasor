---
id: servlet
sidebar_position: 1
title: a.Servlet
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# Servlet

使用 Servlet 如下所示：

```java
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
public class DemoModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        // 扫描所有带有 @MappingTo 注解类
        Set<Class<?>> aClass = apiBinder.findClass(MappingTo.class, "com.example.web.servlet.*");
        // 对 aClass 集合进行发现并自动配置控制器
        apiBinder.loadType(aClass);
    }
}
```
