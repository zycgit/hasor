---
id: once
sidebar_position: 4
title: c. One-Time Events
description: Register event listeners that execute only once.
---

# One-Time Events

One-time events are a special event execution mode. Whether you register synchronous or asynchronous events, the event listener can be registered to execute only once.

This mode is often used when Hasor registers a `ContextEvent_Started` event during init. After the application starts and fires the Started event, the event listener is automatically unregistered.

```java title='The following line registers such a listener'
EventContext eventContext = ...
eventContext.pushListener("EventName",new MyListener());
```
