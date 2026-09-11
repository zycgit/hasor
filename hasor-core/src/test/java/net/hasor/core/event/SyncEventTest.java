/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.event;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.core.EventContext;
import org.junit.Test;

public class SyncEventTest {
    @Test
    public void syncEventTest() throws Throwable {
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        String EventName = "MyEvent";
        //1.添加事件监听器
        final CopyOnWriteArraySet<String> eventDataSet = new CopyOnWriteArraySet<>();
        ec.addListener(EventName, (event, eventData) -> {
            eventDataSet.add(event + eventData);
            Thread.sleep(110);
        });
        //2.引发同步事件
        ArrayList<String> eventData = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            eventData.add(EventName + i);
            ec.fireSyncEvent(EventName, i);
        }
        //
        //3.check事件必须都执行到
        for (String key : eventData) {
            assert eventDataSet.contains(key);
        }
    }

    @Test
    public void onesSyncEventTest() throws Throwable {
        EventContext ec = new StandardEventManager(10, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        String EventName = "MyEvent";
        //1.添加事件监听器
        final CopyOnWriteArraySet<String> eventDataSet = new CopyOnWriteArraySet<>();
        ec.addListener(EventName, (event, eventData) -> {
            eventDataSet.add(event + eventData);
            Thread.sleep(100); // 100ms
        });
        //2.引发异步事件
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            ec.fireSyncEvent(EventName, i);
            assert eventDataSet.contains(EventName + i);
        }
        long endTime = System.currentTimeMillis();
        Thread.sleep(1000);
        //
        //3.check
        assert eventDataSet.size() == 50;// 线程池大小为 10 ，执行完至少要 500ms
        assert (endTime - startTime) > 0;
    }

    @Test
    public void syncTest1() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        String EventName = "MyEvent";
        final Throwable error = new Exception("testError");
        ec.pushListener(EventName, (event, eventData) -> {
            throw error;
        });
        //
        try {
            ec.fireSyncEvent(EventName, null);
            assert false;
        } catch (Exception e) {
            assert e == error;
        }
    }

    @Test
    public void syncTest2() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        final AtomicInteger atomicInteger = new AtomicInteger();
        final ThreadLocal<String> local = new ThreadLocal<>();
        local.set("abc");
        //
        String EventName = "MyEvent";
        ec.addListener(EventName, (event, eventData) -> {
            if ("abc".equals(local.get())) {
                atomicInteger.incrementAndGet();
            }
        });
        //
        //
        ec.fireSyncEventWithAlone(EventName, null);
        assert atomicInteger.get() == 0;
        //
        ec.fireSyncEvent(EventName, null);
        assert atomicInteger.get() == 1;
    }

    @Test
    public void syncTest3() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        final ThreadLocal<Exception> local = new ThreadLocal<>();
        local.set(new Exception("testError"));
        //
        String EventName = "MyEvent";
        ec.addListener(EventName, (event, eventData) -> {
            if (local.get() != null) {
                throw local.get();
            } else {
                throw new Exception("testError2");
            }
        });
        //
        try {
            ec.fireSyncEventWithAlone(EventName, null);
            assert false;
        } catch (Exception e) {
            assert "testError2".equals(e.getMessage());
        }
    }

    @Test
    public void syncTest4() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        //
        try {
            ec.fireSyncEvent(null, null);
            assert false;
        } catch (Exception e) {
            assert "eventType is empty.".equals(e.getMessage());
        }
    }
}
