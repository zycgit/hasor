---
id: custom
sidebar_position: 6
title: 自定义请求
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 自定义请求

通常请求是浏览器发起的，请求类型也是固定的。如果使用了 ajax 框架或者非浏览器发起请求，那么请求类型实际上是可以被修改的。Hasor 支持自定义

例如：接收请求类型为“ABC”方法调用

```java
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("ABC")
public @interface ABC {
}
```

然后在接收请求时指定它

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @ABC
    public void doAbc() {
        ...
    }

    @Get
    public void doGet() {
        ...
    }
}
```
