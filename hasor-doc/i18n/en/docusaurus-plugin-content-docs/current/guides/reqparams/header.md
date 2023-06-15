---
id: header
sidebar_position: 3
title: c.获取请求头
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 获取请求头

获取请求头信息使用 `@HeaderParameter` 注解：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
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
