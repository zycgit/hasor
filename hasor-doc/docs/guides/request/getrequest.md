---
id: getrequest
sidebar_position: 3
title: b.获得Request接口
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 获得Request接口

由于 Request 的存在周期很特殊，因此不能通过 @Inject 方式进行常规的注入。Hasor 提供了一种便捷的方式拿到它：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
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
| `net.hasor.core.Environment`             | 环境接口                |
| `net.hasor.core.Settings`                | 配置相关接口              |
