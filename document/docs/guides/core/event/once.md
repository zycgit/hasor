---
id: once
sidebar_position: 4
title: 2.7.3 执行一次的事件
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.7.3 执行一次的事件

执行一次的事件，这是一类特殊的事件执行方式。无论您注册的是 同步事件 还是 异步事件 都可以将事件监听器注册为只执行一次这种模式。

只执行一次，这种事件通常是用在 Hasor 在 init 过程中注册一个 `ContextEvent_Started` 事件。当应用启动引发 Started 事件之后自动注销事件监听器。

```java title='下面这行代码就是注册方式'
EventContext eventContext = ...
eventContext.pushListener("EventName",new MyListener());
```
