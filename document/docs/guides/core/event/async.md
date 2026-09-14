---
id: async
sidebar_position: 3
title: 2.7.2 异步事件
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.7.2 异步事件

异步事件，异步事件是指当主流程引发事件时，不阻塞主流程。事件的执行完全交给事件线程进行异步调用。

```java
EventContext eventContext = ...
eventContext.fireAsyncEvent(EventName, ...);
```

异步事件有一个参数可以控制执行过程中遇到突发状况下的行为，由 FireType 枚举定义：
- Interrupt（顺序执行所有监听器，如果中途出错，那么终止执行）
- Continue（顺序执行所有监听器，如果中途出错，那么继续执行下一个监听器）

`fireSyncEvent` 方法默认的执行方式是 `Interrupt`，下面是两者的触发方式：

```java
EventContext eventContext = ...
eventContext.fireAsyncEvent(EventName, ... ,FireType.Interrupt);

or

eventContext.fireAsyncEvent(EventName, ... ,FireType.Continue);
```
