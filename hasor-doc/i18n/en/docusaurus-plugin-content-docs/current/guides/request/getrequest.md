---
id: getrequest
sidebar_position: 3
title: b. Obtaining the Request Interface
description: Receive Servlet request-related objects as action method parameters.
---

# Obtaining the Request Interface

Because the lifecycle of a request is special, it cannot be injected normally through `@Inject`. Hasor provides a convenient way to obtain it:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute(HttpServletRequest request) {
        ...
    }
}
```

Types that can be injected into methods include:

| Interface                                | Description                                      |
|------------------------------------------|--------------------------------------------------|
| `javax.servlet.ServletRequest`           | `ServletRequest`                                 |
| `javax.servlet.http.HttpServletRequest`  | `HttpServletRequest`                             |
| `javax.servlet.ServletResponse`          | `ServletResponse`                                |
| `javax.servlet.http.HttpServletResponse` | `HttpServletResponse`                            |
| `javax.servlet.http.HttpSession`         | `HttpSession`                                    |
| `javax.servlet.ServletContext`           | `ServletContext`                                 |
| `net.hasor.web.Invoker` or a custom subtype | Represents one request invocation             |
| `net.hasor.web.render.RenderInvoker`     | `Invoker` extension for page rendering           |
| `net.hasor.web.valid.ValidInvoker`       | `Invoker` extension for form validation          |
| `net.hasor.core.AppContext`              | Container interface                              |
| `net.hasor.cobble.setting.Settings`      | Configuration-related interface                  |
