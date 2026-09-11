/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.dynamic.Aop;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AopCoreIntegrationTest {
    @Test
    public void configDoesNotRegisterAnnotationInterceptionTwice() {
        CountingInterceptor.calls.set(0);
        try (AppContext context = Hasor.create().build()) {
            assertEquals("ok", context.getInstance(Service.class).execute());
            assertEquals(1, CountingInterceptor.calls.get());
        }
    }

    @Aop(CountingInterceptor.class)
    public static class Service {
        public String execute() {
            return "ok";
        }
    }

    public static class CountingInterceptor implements MethodInterceptor {
        static final AtomicInteger calls = new AtomicInteger();

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            calls.incrementAndGet();
            return invocation.proceed();
        }
    }
}
