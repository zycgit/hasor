---
id: cookie
sidebar_position: 2
title: b.获取 Cookie
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 获取 Cookie

:::tip
如果 cookie 中存储了一组相同的数据，那么可以使用 `@CookieParameter("values") String[] vars` 获取。
:::

通过 `@CookieParameter` 注解获取 Cookie 数据，该注解的用法和 `@RequestParameter` 一样：

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
