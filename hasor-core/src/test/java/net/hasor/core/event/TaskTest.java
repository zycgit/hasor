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
import net.hasor.cobble.concurrent.future.FutureCallback;
import net.hasor.core.EventContext;
import org.junit.Test;

public class TaskTest {
    @Test
    public void listenerTest0() throws Throwable {
        //
        EventContext ec = new StandardEventManager(20, "TestEvent", Thread.currentThread().getContextClassLoader());
        final AtomicInteger runInteger = new AtomicInteger(0);
        final AtomicInteger completedInteger = new AtomicInteger(0);
        final AtomicInteger failedInteger = new AtomicInteger(0);
        //
        FutureCallback<Void> futureCallback = new FutureCallback<Void>() {
            @Override
            public void completed(Void result) {
                completedInteger.incrementAndGet();
            }

            @Override
            public void failed(Throwable ex) {
                failedInteger.incrementAndGet();
            }
        };
        class TestRunnable implements Runnable {
            private final boolean b;

            public TestRunnable(boolean b) {
                this.b = b;
            }

            @Override
            public void run() {
                runInteger.incrementAndGet();
                if (!b) {
                    throw new RuntimeException();
                }
            }
        }
        class TestCallable implements Callable<Void> {
            private final boolean b;

            public TestCallable(boolean b) {
                this.b = b;
            }

            @Override
            public Void call() throws Exception {
                runInteger.incrementAndGet();
                if (!b) {
                    throw new RuntimeException();
                }
                return null;
            }
        }
        //
        //
        ec.asyncTask(new TestRunnable(true));
        ec.asyncTask(new TestRunnable(false), futureCallback);
        ec.asyncTask(new TestCallable(true));
        ec.asyncTask(new TestCallable(false), futureCallback);
        //
        //
        //
        Thread.sleep(500);
        assert runInteger.get() == 4;
        assert completedInteger.get() == 0;
        assert failedInteger.get() == 2;
    }
}
