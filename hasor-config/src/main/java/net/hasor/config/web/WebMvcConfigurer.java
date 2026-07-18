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

import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

/**
 * Callback interface for customizing Hasor Web MVC from a {@code @Configuration} class.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-07-18
 */
public interface WebMvcConfigurer extends WebModule {
    /** Configure static resource mappings. */
    default void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /** Configure cross-origin request mappings. */
    default void addCorsMappings(CorsRegistry registry) {
    }

    /** Configure the JSON render engine. */
    default void configureJson(JsonRenderConfigurer configurer) {
    }

    @Override
    default void loadModule(WebApiBinder webBinder) {
        ResourceHandlerRegistry resourceRegistry = new ResourceHandlerRegistry(webBinder);
        this.addResourceHandlers(resourceRegistry);
        resourceRegistry.register();

        CorsRegistry corsRegistry = new CorsRegistry(webBinder);
        this.addCorsMappings(corsRegistry);
        corsRegistry.register();

        JsonRenderConfigurer jsonConfigurer = new JsonRenderConfigurer(webBinder);
        this.configureJson(jsonConfigurer);
        jsonConfigurer.register();
    }
}
