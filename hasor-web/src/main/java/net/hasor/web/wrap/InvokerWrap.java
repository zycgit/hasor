/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.wrap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.function.EFunction;
import net.hasor.core.AppContext;
import net.hasor.web.Invoker;
import net.hasor.web.Mapping;

/**
 * {@link Invoker} 接口包装器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class InvokerWrap implements Invoker {
    protected final Invoker dataContext;

    public InvokerWrap(Invoker dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    public AppContext getAppContext() {
        return this.dataContext.getAppContext();
    }

    @Override
    public HttpServletRequest getHttpRequest() {
        return this.dataContext.getHttpRequest();
    }

    @Override
    public HttpServletResponse getHttpResponse() {
        return this.dataContext.getHttpResponse();
    }

    @Override
    public <T> Future<T> asyncExecute(EFunction<Invoker, T, Throwable> consumer, Executor executor) {
        return this.dataContext.asyncExecute(consumer, executor);
    }

    @Override
    public <T> Future<T> asyncExecute(EFunction<Invoker, T, Throwable> consumer) {
        return this.dataContext.asyncExecute(consumer);
    }

    @Override
    public String contentType() {
        return this.dataContext.contentType();
    }

    @Override
    public void contentType(String contentType) {
        this.dataContext.contentType(contentType);
    }

    @Override
    public Mapping ownerMapping() {
        return this.dataContext.ownerMapping();
    }

    @Override
    public String getJsonBodyString() {
        return this.dataContext.getJsonBodyString();
    }

    @Override
    public <T> T fillForm(Class<? extends T> formType, T bean) {
        return this.dataContext.fillForm(formType, bean);
    }

    @Override
    public boolean isLockKey(String key) {
        return this.dataContext.isLockKey(key);
    }

    @Override
    public void lockKey(String key) {
        this.dataContext.lockKey(key);
    }

    @Override
    public String getMimeType(String suffix) {
        return this.dataContext.getMimeType(suffix);
    }
}
