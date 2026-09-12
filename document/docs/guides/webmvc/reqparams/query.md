---
id: query
sidebar_position: 4
title: 获取URL查询参数
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 获取URL查询参数

需要使用 `@QueryParameter` 注解，例如：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@QueryParameter("value") boolean ajaxTo) {
        ...
    }
}
```

请求URL地址：`http://localhost:8080/helloAction.do?value=true`\
