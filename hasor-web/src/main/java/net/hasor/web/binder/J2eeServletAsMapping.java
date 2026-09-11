/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.provider.SingleProvider;
import net.hasor.core.EventContext;
import net.hasor.core.HasorUtils;
import net.hasor.web.Controller;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Any;

/**
 * 线程安全
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-6-5
 */
public class J2eeServletAsMapping implements Controller {
    private final OneConfig                   initParams;
    private final AtomicBoolean               inited;
    protected     Supplier<? extends Servlet> targetServlet;

    public J2eeServletAsMapping(OneConfig initParams, Supplier<? extends Servlet> j2eeServlet) {
        this.initParams = initParams;
        this.inited = new AtomicBoolean(false);
        this.targetServlet = new SingleProvider<>(j2eeServlet);
    }

    public Supplier<? extends Servlet> getTarget() {
        return targetServlet;
    }

    public ServletConfig getInitParams() {
        return initParams;
    }

    @Override
    public void initController(Invoker invoker) throws ServletException {
        if (!this.inited.compareAndSet(false, true)) {
            return;
        }
        // 初始化
        this.targetServlet.get().init(this.initParams);
        // 注册销毁回调
        EventContext env = invoker.getAppContext().getEventContext();
        HasorUtils.pushShutdownListener(env, (event, eventData) -> {
            destroy();
        });
    }

    /** 执行Servlet */
    @Any
    public void doInvoke(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        if (!this.inited.get()) {
            throw new IllegalStateException("this Servlet uninitialized.");
        }
        this.targetServlet.get().service(request, response);
    }

    /** 销毁过滤器。 */
    public void destroy() {
        if (!this.inited.compareAndSet(true, false)) {
            return;
        }
        this.targetServlet.get().destroy();
    }
}
