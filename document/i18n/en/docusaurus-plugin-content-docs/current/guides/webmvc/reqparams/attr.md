---
id: attr
sidebar_position: 5
title: e. Reading Attributes
description: Read request attributes with @AttributeParameter.
---

# Reading Attributes

The most basic way is to use `httpRequest.getAttribute`, but Hasor provides the `@AttributeParameter` annotation.

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@AttributeParameter("value") boolean value) {
        ...
    }
}
```
