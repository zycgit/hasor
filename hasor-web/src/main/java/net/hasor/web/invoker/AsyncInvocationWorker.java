/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.lang.reflect.Method;
import javax.servlet.AsyncContext;

/**
 * Servlet 3 异步请求处理
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-13
 */
public abstract class AsyncInvocationWorker implements Runnable {
    private final AsyncContext asyncContext;
    private final Method       targetMethod;

    public AsyncInvocationWorker(AsyncContext asyncContext, Method targetMethod) {
        this.asyncContext = asyncContext;
        this.targetMethod = targetMethod;
    }

    @Override
    public void run() {
        try {
            this.doWork(this.targetMethod);
        } catch (Throwable e) {
            this.doWorkWhenError(this.targetMethod, e);
        } finally {
            this.asyncContext.complete();
        }
    }

    public abstract void doWork(Method targetMethod) throws Throwable;

    public abstract void doWorkWhenError(Method targetMethod, Throwable e);
}
