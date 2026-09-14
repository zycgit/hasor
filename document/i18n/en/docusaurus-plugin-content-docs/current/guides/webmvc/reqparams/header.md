---
id: header
sidebar_position: 3
title: 4.3.3 Reading Request Headers
description: Read request headers with @HeaderParameter.
---

# 4.3.3 Reading Request Headers

Use the `@HeaderParameter` annotation to read request header information:

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
