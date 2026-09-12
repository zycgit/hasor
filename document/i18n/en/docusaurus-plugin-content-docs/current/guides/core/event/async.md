---
id: async
sidebar_position: 3
title: Asynchronous Events
description: Fire asynchronous Hasor events without blocking the main flow.
---

# Asynchronous Events

Asynchronous events do not block the main flow when the main flow fires an event. Event execution is completely delegated to event threads and invoked asynchronously.

```java
EventContext eventContext = ...
eventContext.fireAsyncEvent(EventName, ...);
```

Asynchronous events have a parameter that controls behavior when unexpected conditions occur during execution. It is defined by the `FireType` enum:
- `Interrupt`: execute all listeners in order; stop if an error occurs.
- `Continue`: execute all listeners in order; continue to the next listener if an error occurs.

The default execution mode of `fireSyncEvent` is `Interrupt`. The following shows both trigger methods:

```java
EventContext eventContext = ...
eventContext.fireAsyncEvent(EventName, ... ,FireType.Interrupt);

or

eventContext.fireAsyncEvent(EventName, ... ,FireType.Continue);
```
