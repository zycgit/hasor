/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.event;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.core.EventContext;
import net.hasor.core.EventListener;
import org.junit.Test;

public class EventLinkTest {
    @Test
    public void syncEventTest() throws InterruptedException {
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        final String SubEvent = "SubEvent";
        final String FirstEvent = "FirstEvent";
        final AtomicInteger atomicInteger = new AtomicInteger();
        //1.添加事件监听器
        ec.addListener(SubEvent, (event, eventData) -> atomicInteger.incrementAndGet());
        ec.addListener(FirstEvent, (EventListener<EventContext>) (event, eventEC) -> {
            eventEC.fireAsyncEvent(SubEvent, 1);
            eventEC.fireAsyncEvent(SubEvent, 2);
        });
        //2.引发种子事件
        ec.fireAsyncEvent(FirstEvent, ec);
        Thread.sleep(1000);
        assert atomicInteger.get() == 2;
    }
}
