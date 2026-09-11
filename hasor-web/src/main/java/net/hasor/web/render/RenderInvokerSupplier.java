/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.wrap.InvokerWrap;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class RenderInvokerSupplier extends InvokerWrap implements RenderInvoker {
    private String  viewName   = null;    // 模版名称
    private String  renderType = null;    // 渲染引擎
    private boolean useLayout  = true;    // 是否渲染布局

    protected RenderInvokerSupplier(Invoker invoker) {
        super(invoker);
        //
        HttpServletRequest httpRequest = this.getHttpRequest();
        Enumeration<?> paramEnum = httpRequest.getParameterNames();
        while (paramEnum != null && paramEnum.hasMoreElements()) {
            Object paramKey = paramEnum.nextElement();
            String key = paramKey.toString();
            String val = httpRequest.getParameter(key);
            httpRequest.setAttribute("req_" + key, val);
        }
    }

    @Override
    public String renderTo() {
        return this.viewName;
    }

    @Override
    public void renderTo(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public void renderTo(String renderType, String viewName) {
        this.renderType(renderType);
        this.viewName = viewName;
    }

    @Override
    public String renderType() {
        return this.renderType;
    }

    @Override
    public void renderType(String renderType) {
        if (StringUtils.isBlank(renderType)) {
            throw new IllegalStateException("renderType is null.");
        } else {
            this.renderType = renderType.trim().toUpperCase();
        }
    }

    @Override
    public boolean layout() {
        return this.useLayout;
    }

    @Override
    public void layoutEnable() {
        this.useLayout = true;
    }

    @Override
    public void layoutDisable() {
        this.useLayout = false;
    }
}
