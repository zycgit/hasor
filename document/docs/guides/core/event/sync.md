---
id: sync
sidebar_position: 2
title: 2.7.1 同步事件
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.7.1 同步事件

同步事件是指当主流程引发事件时，是否阻塞主流程执行，等待所有事件监听器都执行完毕之后在恢复主流程的执行。主流程的调用等待事件执行完毕，根据执行事件监听器线程模型的不同还可以分为：
- 独享线程：指的是当 Hasor 开始执行事件监听器时，使用一个全新的线程去执行监听器。
- 共享线程：指的是当 Hasor 开始执行事件监听器时，使用当前线程执行监听器。

```java
// 独享线程
EventContext eventContext = ...
eventContext.fireSyncEventWithAlone(EventName, ...);

// 共享线程
EventContext eventContext = ...
eventContext.fireSyncEvent(EventName, ...);
```

:::tip
独享线程下，如果大量的事件抛出会导致 事件调度线程池线程不够用。此时可以通过修改配置来增加处理线程，默认执行事件线程池只有 8 个最大线程。
- 通过 `hasor.eventThreadPoolSize` 配置线程数。
- 配置值可以写成 `${HASOR_LOAD_EVENT_POOL:8}`，再通过 `-DHASOR_LOAD_EVENT_POOL=16` 在启动时指定。
:::
