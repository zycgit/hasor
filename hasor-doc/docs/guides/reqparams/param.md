---
id: param
sidebar_position: 1
title: a.获取请求参数
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 获取请求参数

:::tip
如果页面上使用了 checkbox 来表示一组值，那么可以使用 `@RequestParameter("values") String[] vars` 获取。
:::

通过 `@RequestParameter` 注解，获取请求参数：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute(@RequestParameter("name") String userName,
            @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

请求URL地址：`http://localhost:8080/helloAction.do?name=userA&pwd=123456`
