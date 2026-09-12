---
id: param
sidebar_position: 1
title: Reading Request Parameters
description: Read request parameters with @RequestParameter.
---

# Reading Request Parameters

:::tip
If a page uses checkboxes to represent a group of values, use `@RequestParameter("values") String[] vars` to read them.
:::

Use the `@RequestParameter` annotation to read request parameters:

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

Request URL: `http://localhost:8080/helloAction.do?name=userA&pwd=123456`
