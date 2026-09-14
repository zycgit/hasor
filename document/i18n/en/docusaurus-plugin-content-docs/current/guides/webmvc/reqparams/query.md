---
id: query
sidebar_position: 4
title: 4.3.4 Reading URL Query Parameters
description: Read URL query parameters with @QueryParameter.
---

# 4.3.4 Reading URL Query Parameters

Use the `@QueryParameter` annotation. For example:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@QueryParameter("value") boolean ajaxTo) {
        ...
    }
}
```

Request URL: `http://localhost:8080/helloAction.do?value=true`
