---
id: request
sidebar_position: 1
title: Receiving Web Requests
description: Receive web requests with Hasor Web.
---

# Receiving Web Requests

The simplest form for receiving a web request is shown below:

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    public void execute() {
        ...
    }
}
```
