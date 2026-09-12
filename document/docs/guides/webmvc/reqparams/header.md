---
id: header
sidebar_position: 3
title: 获取请求头
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 获取请求头

获取请求头信息使用 `@HeaderParameter` 注解：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@HeaderParameter("ajaxTo") boolean ajaxTo) {
        ...
    }
}
```

```js
$.ajax({
    beforeSend: function (request) {
        request.setRequestHeader("ajaxTo", "true");
    },
    url: "/helloAcrion.do",
    data: formData,
    dataType: 'json',
    async: true,
    success: function (result) {
        ...
    },
    error: function (result) {
        ...
    }
});
```
