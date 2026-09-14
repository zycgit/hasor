---
id: getrequest
sidebar_position: 3
title: 4.2.2 获得Request接口
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.2.2 获得Request接口

由于 Request 的存在周期很特殊，因此不能通过 @Inject 方式进行常规的注入。Hasor 提供了一种便捷的方式拿到它：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(HttpServletRequest request) {
        ...
    }
}
```

可以在方法中被注入的类型有：

| 接口                                       | 说明                  |
|------------------------------------------|---------------------|
| `javax.servlet.ServletRequest`           | ServletRequest      |
| `javax.servlet.http.HttpServletRequest`  | HttpServletRequest  |
| `javax.servlet.ServletResponse`          | ServletResponse     |
| `javax.servlet.http.HttpServletResponse` | HttpServletResponse |
| `javax.servlet.http.HttpSession`         | HttpSession         |
| `javax.servlet.ServletContext`           | ServletContext      |
| `net.hasor.web.Invoker` 或自定义扩展子类型        | 用来表示一次请求调用          |
| `net.hasor.web.render.RenderInvoker`     | Invoker 扩展，用来处理页面渲染 |
| `net.hasor.web.valid.ValidInvoker`       | Invoker 扩展，用来处理表单验证 |
| `net.hasor.core.AppContext`              | 容器接口                |
| `net.hasor.cobble.setting.Settings`      | 配置相关接口              |
