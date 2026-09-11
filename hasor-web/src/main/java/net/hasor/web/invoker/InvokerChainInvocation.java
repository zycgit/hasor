/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.binder.FilterDef;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
class InvokerChainInvocation implements InvokerChain {
    private final FilterDef[]  filters;
    private final InvokerChain chain;
    private       int          index = -1;

    public InvokerChainInvocation(final FilterDef[] filters, final InvokerChain chain) {
        this.filters = filters;
        this.chain = chain;
    }

    @Override
    public Object doNext(Invoker invoker) throws Throwable {
        this.index++;
        if (this.index < this.filters.length) {
            if (this.filters[this.index].matchesInvoker(invoker)) {
                return this.filters[this.index].doInvoke(invoker, this);
            } else {
                return this.doNext(invoker);
            }
        } else {
            return this.chain.doNext(invoker);
        }
    }
}
