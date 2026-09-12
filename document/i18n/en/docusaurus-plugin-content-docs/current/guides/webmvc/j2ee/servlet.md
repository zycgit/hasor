---
id: servlet
sidebar_position: 1
title: Servlet
description: Register and use Servlets in Hasor Web.
---

# Servlet

Use a servlet as shown below:

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

Then register the servlet.

```java
public class DemoModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        // Scan all classes annotated with @MappingTo.
        Set<Class<?>> aClass = apiBinder.findClass(MappingTo.class, "com.example.web.servlet.*");
        // Discover the aClass collection and configure Servlets automatically.
        apiBinder.loadMappingTo(aClass);
    }
}
```
