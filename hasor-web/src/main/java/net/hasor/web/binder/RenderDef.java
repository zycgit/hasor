/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.web.render.RenderEngine;

/**
 * 渲染引擎定义。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class RenderDef {
    private final String                           renderName;
    private final BindInfo<? extends RenderEngine> bindInfo;
    private final boolean                          fallback;

    public RenderDef(String renderName, BindInfo<? extends RenderEngine> bindInfo) {
        this(renderName, bindInfo, false);
    }

    RenderDef(String renderName, BindInfo<? extends RenderEngine> bindInfo, boolean fallback) {
        this.fallback = fallback;
        this.renderName = renderName;
        this.bindInfo = bindInfo;
    }

    /** Configured defaults are used only when no explicit engine has the same name. */
    public boolean isFallback() {
        return this.fallback;
    }

    @Override
    public String toString() {
        return String.format("rendName=%s ,toBindID=%s", this.renderName, this.bindInfo.getBindID());
    }

    public String getID() {
        return this.bindInfo.getBindID();
    }

    public String getRenderName() {
        return this.renderName;
    }

    public RenderEngine newEngine(AppContext appContext) throws Throwable {
        return appContext.getInstance(this.bindInfo);
    }
}
