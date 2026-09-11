/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.filters;
import java.io.IOException;
import javax.servlet.*;

public class SimpleFilter implements Filter {
    private boolean      init;
    private boolean      destroy;
    private boolean      doCall;
    private FilterConfig config;

    public boolean isInit() {
        return init;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public boolean isDoCall() {
        return doCall;
    }

    public FilterConfig getConfig() {
        return config;
    }

    @Override
    public void init(FilterConfig config) {
        this.init = true;
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doCall = true;
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        this.destroy = true;
    }
}
