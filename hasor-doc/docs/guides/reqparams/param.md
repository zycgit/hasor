---
id: param
sidebar_position: 1
title: a.获取请求参数
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 获取请求参数

:::tip
如果页面上使用了 checkbox 来表示一组值，那么可以使用 `@RequestParameter("values") String[] vars` 获取。
:::

通过 `@RequestParameter` 注解，获取请求参数：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@RequestParameter("name") String userName,
            @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

请求URL地址：`http://localhost:8080/helloAction.do?name=userA&pwd=123456`
