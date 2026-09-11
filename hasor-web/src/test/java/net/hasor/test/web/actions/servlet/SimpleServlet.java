/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.servlet;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-08
 */
public class SimpleServlet extends HttpServlet {
    private boolean       init;
    private boolean       destroy;
    private boolean       doCall;
    private ServletConfig config;

    public boolean isInit() {
        return init;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public boolean isDoCall() {
        return doCall;
    }

    public ServletConfig getConfig() {
        return config;
    }

    public void init(ServletConfig config) {
        this.init = true;
        this.config = config;
    }

    @Override
    public void destroy() {
        this.destroy = true;
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        this.doCall = true;
    }
}
