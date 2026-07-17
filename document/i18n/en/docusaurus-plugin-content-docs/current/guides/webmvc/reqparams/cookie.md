---
id: cookie
sidebar_position: 2
title: b. Reading Cookies
description: Read cookie values with @CookieParameter.
---

# Reading Cookies

:::tip
If a cookie stores a group of values with the same name, use `@CookieParameter("values") String[] vars` to read them.
:::

Use the `@CookieParameter` annotation to read cookie data. Its usage is the same as `@RequestParameter`:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@CookieParameter("name") String userName,
            @CookieParameter("pwd") String pwd) {
        ...
    }
}
```
