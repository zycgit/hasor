---
id: group
sidebar_position: 6
title: f.请求参数组
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 请求参数组

当一个请求递交了大量参数时，为了减少编写参数列表可以使用 `@ParameterGroup`

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute(@ParameterGroup() UserInfo userInfo) {
        ...
    }
}

public class UserInfo {
    @RequestParameter("param_1")
    private String param_1;
    @RequestParameter("param_2")
    private String param_2;
    @RequestParameter("param_3")
    private String param_3;
    @RequestParameter("param_4")
    private String param_4;
    @RequestParameter("param_5")
    private String param_5;
    ...
}
```

前端如果递交的内容是一个 JSON ，还可以通过 @RequestBody 获取请求Body。这在当下比较常见。

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Post
    public void execute(@RequestBody() Map<String, Object> requestBody) {
        ...
    }
}
```