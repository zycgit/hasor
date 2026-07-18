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
package net.hasor.config.web;

import java.util.Objects;
import java.util.function.Supplier;
import net.hasor.cobble.StringUtils;
import net.hasor.web.WebApiBinder;
import net.hasor.web.binder.RenderDef;
import net.hasor.web.objects.JsonRenderEngine;
import net.hasor.web.render.RenderEngine;

/** Configures the Web MVC JSON renderer. */
public class JsonRenderConfigurer {
    private final WebApiBinder                     webBinder;
    private       String                           renderName = "json";
    private       Class<? extends RenderEngine>    renderEngineType;
    private       Supplier<? extends RenderEngine> renderEngine;

    JsonRenderConfigurer(WebApiBinder webBinder) {
        this.webBinder = Objects.requireNonNull(webBinder, "webBinder is null.");
    }

    /** Enables the built-in Fastjson renderer. */
    public JsonRenderConfigurer useDefaultJsonRenderEngine() {
        return this.renderEngine(JsonRenderEngine.class);
    }

    public JsonRenderConfigurer renderName(String renderName) {
        if (StringUtils.isBlank(renderName)) {
            throw new IllegalArgumentException("renderName is blank.");
        }
        this.renderName = renderName.trim();
        return this;
    }

    public JsonRenderConfigurer renderEngine(RenderEngine renderEngine) {
        Objects.requireNonNull(renderEngine, "renderEngine is null.");
        return this.renderEngine(() -> renderEngine);
    }

    /** Uses a Hasor-managed render engine type. */
    public JsonRenderConfigurer renderEngine(Class<? extends RenderEngine> renderEngineType) {
        this.renderEngineType = Objects.requireNonNull(renderEngineType, "renderEngineType is null.");
        this.renderEngine = null;
        return this;
    }

    public JsonRenderConfigurer renderEngine(Supplier<? extends RenderEngine> renderEngine) {
        this.renderEngine = Objects.requireNonNull(renderEngine, "renderEngine is null.");
        this.renderEngineType = null;
        return this;
    }

    void register() {
        if (this.renderEngineType == null && this.renderEngine == null) {
            return;
        }
        if (this.webBinder.findBindingRegister(this.renderName, RenderDef.class) != null) {
            throw new IllegalStateException("Render engine '" + this.renderName + "' is already configured.");
        }
        if (this.renderEngineType != null) {
            this.webBinder.addRender(this.renderName).to(this.renderEngineType);
        } else {
            this.webBinder.addRender(this.renderName).toProvider(this.renderEngine);
        }
    }
}
