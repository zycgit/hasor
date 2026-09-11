/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.event;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.core.EventContext;
import net.hasor.core.EventListener;
import org.junit.Test;

public class ListenerTest {
    @Test
    public void listenerTest0() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        final String FirstEvent = "FirstEvent";
        final AtomicInteger atomicInteger = new AtomicInteger();
        final EventListener<EventContext> listener = (event, eventEC) -> atomicInteger.incrementAndGet();
        //
        //
        assert ec.addListener(FirstEvent, listener);
        ec.fireSyncEvent(FirstEvent, ec);
        assert ec.removeListener(FirstEvent, listener);
        //
        ec.fireSyncEvent(FirstEvent, ec);
        assert atomicInteger.get() == 1;
    }

    @Test
    public void listenerTest1() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        final String FirstEvent = "FirstEvent";
        final AtomicInteger atomicInteger = new AtomicInteger();
        final EventListener<EventContext> listener = (event, eventEC) -> atomicInteger.incrementAndGet();
        //
        //
        assert ec.addListener(FirstEvent, listener);
        ec.fireSyncEvent(FirstEvent, ec);
        assert ec.clearListener(FirstEvent);
        //
        ec.fireSyncEvent(FirstEvent, ec);
        assert atomicInteger.get() == 1;
    }

    @Test
    public void listenerTest2() throws Throwable {
        //
        StandardEventManager ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        //
        assert !ec.pushListener(null, null);
        assert !ec.addListener(null, null);
        assert !ec.removeListener(null, null);
        assert !ec.clearListener(null);
        //
        assert ec.asyncTask((Runnable) null) == null;
        assert ec.asyncTask((Callable) null) == null;
        //
        assert !ec.asyncTask((Runnable) null, null);
        assert !ec.asyncTask((Callable) null, null);
        //
        ec.release();
    }
}
