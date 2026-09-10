/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.binder;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.web.render.RenderEngine;

/**
 * 渲染引擎定义。
 * @version : 2017-01-10
 * @author 赵永春 (zyc@hasor.net)
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
