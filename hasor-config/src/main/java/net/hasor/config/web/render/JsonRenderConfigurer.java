/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web.render;
import java.util.Objects;
import java.util.function.Supplier;
import net.hasor.cobble.StringUtils;
import net.hasor.web.WebApiBinder;
import net.hasor.web.binder.RenderDef;
import net.hasor.web.render.RenderEngine;

/** Configures the Web MVC JSON renderer. */
public class JsonRenderConfigurer {
    private final WebApiBinder                     webBinder;
    private       String                           renderName = "json";
    private       Class<? extends RenderEngine>    renderEngineType;
    private       Supplier<? extends RenderEngine> renderEngine;

    public JsonRenderConfigurer(WebApiBinder webBinder) {
        this.webBinder = Objects.requireNonNull(webBinder, "webBinder is null.");
    }

    public JsonRenderConfigurer renderEngine(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is blank.");
        }

        this.renderName = name.trim();
        return this;
    }

    public JsonRenderConfigurer renderEngine(RenderEngine engine) {
        Objects.requireNonNull(engine, "engine is null.");
        return this.renderEngine(() -> engine);
    }

    /** Uses a Hasor-managed render engine type. */
    public JsonRenderConfigurer renderEngine(Class<? extends RenderEngine> engine) {
        this.renderEngineType = Objects.requireNonNull(engine, "engine is null.");
        this.renderEngine = null;
        return this;
    }

    public JsonRenderConfigurer renderEngine(Supplier<? extends RenderEngine> engine) {
        this.renderEngine = Objects.requireNonNull(engine, "engine is null.");
        this.renderEngineType = null;
        return this;
    }

    public void register() {
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
