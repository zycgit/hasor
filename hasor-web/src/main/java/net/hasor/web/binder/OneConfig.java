/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import net.hasor.cobble.CollectionUtils;
import net.hasor.core.AppContext;
import net.hasor.web.InvokerConfig;

/**
 * Abstract implementation for all servlet module bindings
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-4-12
 */
public class OneConfig extends HashMap<String, String> implements FilterConfig, ServletConfig, InvokerConfig {
    private String               resourceName;
    private Supplier<AppContext> appContext;

    public OneConfig() {
    }

    public OneConfig(String resourceName, Supplier<AppContext> appContext) {
        this();
        this.resourceName = resourceName;
        this.appContext = appContext;
    }

    public OneConfig(String resourceName, Map<String, String> initParams, Supplier<AppContext> appContext) {
        this();
        this.resourceName = resourceName;
        this.appContext = appContext;
        if (initParams != null) {
            this.putAll(initParams);
        }
    }

    public OneConfig(FilterConfig config, Supplier<AppContext> appContext) {
        this();
        this.resourceName = config.getFilterName();
        this.appContext = appContext;
        this.putConfig(config, true);
    }

    public OneConfig(ServletConfig config, Supplier<AppContext> appContext) {
        this();
        this.resourceName = config.getServletName();
        this.appContext = appContext;
        this.putConfig(config, true);
    }

    public OneConfig(String resourceName, InvokerConfig config, Supplier<AppContext> appContext) {
        this();
        this.resourceName = resourceName;
        this.appContext = appContext;
        this.putConfig(config, true);
    }

    public void putConfig(FilterConfig config, boolean overwrite) {
        Enumeration<?> names = config.getInitParameterNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement().toString();
                this.computeIfAbsent(name, s -> overwrite ? config.getInitParameter(name) : s);
            }
        }
    }

    public void putConfig(ServletConfig config, boolean overwrite) {
        Enumeration<?> names = config.getInitParameterNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement().toString();
                this.computeIfAbsent(name, s -> overwrite ? config.getInitParameter(name) : s);
            }
        }
    }

    public void putConfig(InvokerConfig config, boolean overwrite) {
        Enumeration<?> names = config.getInitParameterNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement().toString();
                this.computeIfAbsent(name, s -> overwrite ? config.getInitParameter(name) : s);
            }
        }
    }

    @Override
    public String getFilterName() {
        return this.resourceName;
    }

    @Override
    public String getServletName() {
        return this.resourceName;
    }

    @Override
    public ServletContext getServletContext() {
        if (this.appContext != null) {
            AppContext appContext = this.appContext.get();
            return appContext.getInstance(ServletContext.class);
        }
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return this.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return CollectionUtils.asEnumeration(OneConfig.this.keySet().iterator());
    }

    @Override
    public AppContext getAppContext() {
        return this.appContext.get();
    }
}
