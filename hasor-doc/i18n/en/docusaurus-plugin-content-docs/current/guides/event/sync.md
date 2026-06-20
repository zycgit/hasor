---
id: sync
sidebar_position: 2
title: a. Synchronous Events
description: Fire synchronous Hasor events with shared or dedicated threads.
---

# Synchronous Events

A synchronous event determines whether the main flow blocks when it fires the event and waits until all event listeners have finished before continuing. Depending on the listener thread model, synchronous events can be divided into:
- Dedicated thread: when Hasor starts executing event listeners, it uses a new thread to execute them.
- Shared thread: when Hasor starts executing event listeners, it uses the current thread to execute them.

```java
// Dedicated thread.
EventContext eventContext = ...
eventContext.fireSyncEventWithEspecial(EventName, ...);

// Shared thread.
EventContext eventContext = ...
eventContext.fireSyncEvent(EventName, ...);
```

:::tip
With dedicated threads, firing a large number of events may exhaust the event-dispatch thread pool. Increase the number of processing threads through configuration. The default event thread pool has only 8 maximum threads.
- Configure the thread count with `hasor.eventThreadPoolSize`.
- The configuration value can be written as `${HASOR_LOAD_EVENT_POOL:8}` and specified at startup with `-DHASOR_LOAD_EVENT_POOL=16`.
:::
