---
id: event
sidebar_position: 1
title: 事件模型
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 事件模型

在代码层面往往多个系统之间还会有直接或者间接的调用，为了降低代码耦合度可以使用 Hasor 的事件机制来进行深度解耦。

Hasor 事件的执行分为三种，它们的执行模型如下：
- 同步(独享线程)
- 同步(共享线程)
- 异步

![事件模型](../_img/CC2_950A_FEDD_45ED.png)

无论是同步的事件模型，还是异步事件模型。在 Hasor 事件体系中，它们都有以下共同性质：
- 按注册顺序执行事件监听器
- 事件监听器接口相同
- 事件注册方式相同

## 注册事件监听器

```java title='实现一个监听器'
import net.hasor.core.EventListener;
public class MyListener implements EventListener<Object> {
    public void onEvent(String event, Object eventData) throws InterruptedException {
        Thread.sleep(500);
        System.out.println("Receive Message:" + JSON.toJSONString(eventData));
    }
}
```

```java title='获取 EventContext 接口'
ApiBinder apiBinder = ... 
EventContext ec = apiBinder.getEventContext();

or

AppContext appContext = ...;
EventContext eventContext = appContext.getInstance(EventContext.class);

or

EventContext eventContext = appContext.getEventContext();

or

public class MyBean {
    @Inject
    private EventContext eventContext;
}
```

接着我们通过 EventContext 将事件注册到容器中。

```java title='注册事件监听器'
EventContext eventContext = ...
eventContext.addListener("EventName",new MyListener());
```

## 引发事件

```java title='例如'
eventContext.fireSyncEvent("EventName",...);
```
