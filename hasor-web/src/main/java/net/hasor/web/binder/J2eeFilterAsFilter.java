/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.io.IOException;
import java.util.function.Supplier;
import javax.servlet.*;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.provider.SingleProvider;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerConfig;
import net.hasor.web.InvokerFilter;

/**
 * Filter 转换为 InvokerFilter
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class J2eeFilterAsFilter implements InvokerFilter, Filter {
    protected Supplier<? extends Filter> j2eeFilter = null;

    public J2eeFilterAsFilter(Supplier<? extends Filter> j2eeFilter) {
        this.j2eeFilter = new SingleProvider<>(j2eeFilter);
    }

    public final void init(InvokerConfig config) throws Throwable {
        this.init((FilterConfig) new OneConfig(this.getClass().getName(), config, config::getAppContext));
    }

    @Override
    public final Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        this.doFilter(invoker.getHttpRequest(), invoker.getHttpResponse(), (request, response) -> {
            try {
                chain.doNext(invoker);
            } catch (IOException | ServletException e) {
                throw e;
            } catch (Throwable e) {
                throw ExceptionUtils.toRuntime(e);
            }
        });
        return invoker.get(Invoker.RETURN_DATA_KEY);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.j2eeFilter.get().init(filterConfig);
    }

    @Override
    public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        this.j2eeFilter.get().doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {
        this.j2eeFilter.get().destroy();
    }
}
