/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web;
import net.hasor.config.web.cors.CorsRegistry;
import net.hasor.config.web.render.JsonRenderConfigurer;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

/**
 * Callback interface for customizing Hasor Web MVC from a {@code @Configuration} class.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-07-18
 */
public interface WebMvcConfigurer extends WebModule {
    @Override
    default void loadModule(WebApiBinder webBinder) {
        this.addResourceHandlers(webBinder);

        CorsRegistry corsRegistry = new CorsRegistry(webBinder);
        this.addCorsMappings(corsRegistry);
        corsRegistry.register();

        JsonRenderConfigurer jsonConfigurer = new JsonRenderConfigurer(webBinder);
        this.configureJson(jsonConfigurer);
        jsonConfigurer.register();
    }

    /** Configure static resource mappings. */
    default void addResourceHandlers(WebApiBinder binder) {
    }

    /** Configure cross-origin request mappings. */
    default void addCorsMappings(CorsRegistry registry) {
    }

    /** Configure the JSON render engine. */
    default void configureJson(JsonRenderConfigurer configurer) {
    }
}
