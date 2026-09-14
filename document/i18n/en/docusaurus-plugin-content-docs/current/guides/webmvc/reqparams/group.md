---
id: group
sidebar_position: 6
title: 4.3.6 Request Parameter Groups
description: Bind many request parameters into a grouped object.
---

# 4.3.6 Request Parameter Groups

When a request submits many parameters, use `@ParameterGroup` to reduce long parameter lists.

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
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

If the frontend submits JSON content, you can also use `@RequestBody` to read the request body. This is common today.

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Post
    public void execute(@RequestBody() Map<String, Object> requestBody) {
        ...
    }
}
```
