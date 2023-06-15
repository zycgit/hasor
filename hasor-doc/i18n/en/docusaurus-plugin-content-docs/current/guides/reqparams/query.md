---
id: query
sidebar_position: 4
title: d.获取URL查询参数
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 获取URL查询参数

需要使用 `@QueryParameter` 注解，例如：

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute(@QueryParameter("value") boolean ajaxTo) {
        ...
    }
}
```

请求URL地址：`http://localhost:8080/helloAction.do?value=true`\
