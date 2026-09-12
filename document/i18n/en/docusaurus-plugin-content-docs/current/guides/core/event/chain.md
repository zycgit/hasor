---
id: chain
sidebar_position: 5
title: Event Chains
description: Fire one or more events from inside another event.
---

# Event Chains

An event chain means firing one or more additional events from inside an event. A complete event-chain example is shown below:

```java
public class MyListener implements EventListener<Object> {
    public void onEvent(String event, Object eventData) throws InterruptedException {
        Thread.sleep(500);
        System.out.println("Receive Message:" + JSON.toJSONString(eventData));
        throw new NullPointerException();
    }
}

public class EventLinkTest {
    @Test
    public void syncEventTest() throws InterruptedException {
        System.out.println("--->>syncEventTest<<--");
        AppContext appContext = Hasor.create().build();
        EventContext ec = appContext.getEventContext();

        //
        final String EventName = "MyEvent"; // The terminal event in the event chain.
        final String SeedEvent = "SeedEvent"; // Seed event.

        // 1. Add event listeners.
        ec.addListener(EventName, new MyListener());
        ec.addListener(SeedEvent, new EventListener<AppContext>() {
            public void onEvent(String event, AppContext app) throws Throwable {
                EventContext localEC = app.getEventContext();
                System.out.println("before MyEvent.");
                localEC.fireAsyncEvent(EventName, 1);
                localEC.fireAsyncEvent(EventName, 2);
            }
        });

        // 2. Fire the seed event.
        ec.fireAsyncEvent(SeedEvent, appContext);

        // 3. Because this is an asynchronous event, this log is printed before all event processing.
        System.out.println("before All Event.");
        Thread.sleep(1000);
    }
}
```
