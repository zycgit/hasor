/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.filters;
import net.hasor.core.Singleton;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerConfig;
import net.hasor.web.InvokerFilter;

@Singleton
public class SimpleInvokerFilter implements InvokerFilter {
    private boolean       init;
    private boolean       destroy;
    private boolean       doCall;
    private InvokerConfig config;

    public boolean isInit() {
        return init;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public boolean isDoCall() {
        return doCall;
    }

    public InvokerConfig getConfig() {
        return config;
    }

    public void init(InvokerConfig config) {
        this.init = true;
        this.config = config;
    }

    @Override
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        doCall = true;
        return chain.doNext(invoker);
    }

    @Override
    public void destroy() {
        this.destroy = true;
    }
}
