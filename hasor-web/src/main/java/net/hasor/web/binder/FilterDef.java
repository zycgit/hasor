/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerConfig;
import net.hasor.web.InvokerFilter;

/**
 * Abstract implementation for all servlet module bindings
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-12
 */
public class FilterDef implements InvokerFilter {
    private final int                     index;
    private final UriPatternMatcher       patternMatcher;
    private final OneConfig               initParams;
    //
    private final AtomicBoolean           inited;
    private final BindInfo<?>             targetType;
    private final Supplier<InvokerFilter> targetFilter;

    public FilterDef(int index, UriPatternMatcher patternMatcher, Map<String, String> initParams,//
            BindInfo<? extends InvokerFilter> bindInfo, Supplier<AppContext> appContext//
    ) {
        this.index = index;
        this.patternMatcher = patternMatcher;
        this.initParams = new OneConfig(bindInfo.getBindID(), initParams, appContext);
        this.inited = new AtomicBoolean(false);
        this.targetType = bindInfo;
        this.targetFilter = () -> appContext.get().getInstance(bindInfo);
    }

    /***/
    public int getIndex() {
        return this.index;
    }

    /** Returns true if the given URI will match this binding. */
    public boolean matchesInvoker(Invoker invoker) {
        String url = invoker.getRequestPath();
        return this.patternMatcher.matches(url);
    }

    public BindInfo<?> getTargetType() {
        return this.targetType;
    }

    public UriPatternMatcher getMatcher() {
        return patternMatcher;
    }

    public InvokerConfig getInitParams() {
        return initParams;
    }

    @Override
    public String toString() {
        return String.format("pattern=%s ,uriPatternType=%s ,type %s ,initParams=%s ", //
                this.patternMatcher.getPattern(), this.patternMatcher, this.getClass(), this.initParams);
    }

    @Override
    public final void init(InvokerConfig config) throws Throwable {
        if (!this.inited.compareAndSet(false, true)) {
            return;
        }
        if (config != null) {
            this.initParams.putConfig(config, true);
        }
        this.targetFilter().init(this.initParams);
    }

    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        if (!this.inited.get()) {
            throw new IllegalStateException("this Filter uninitialized.");
        }
        //
        return this.targetFilter().doInvoke(invoker, chain);
    }

    private InvokerFilter targetFilter() {
        InvokerFilter filter;
        try {
            filter = this.targetFilter.get();
        } catch (NullPointerException e) {
            throw new NullPointerException("target InvokerFilter instance is null.");
        }
        if (filter == null) {
            throw new NullPointerException("target InvokerFilter instance is null.");
        }
        return filter;
    }

    public void destroy() {
        if (!this.inited.compareAndSet(true, false)) {
            return;
        }
        this.targetFilter.get().destroy();
    }
}
