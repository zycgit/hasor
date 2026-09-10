
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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.loader.providers.PrefixResourceLoader;
import net.hasor.cobble.setting.Settings;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

/** Explicit, container-independent opt-in to standard Web defaults. */
public final class WebDefaultsModule implements WebModule {
    private final WebOptions config;

    public WebDefaultsModule(WebOptions options) {
        this.config = Objects.requireNonNull(options, "options").copy();
    }

    public WebDefaultsModule(Settings settings) {
        this(loadOptions(new WebOptions(), settings));
    }

    /** 将 Config 配置覆盖到给定选项，未配置的值保持不变。 */
    public static WebOptions loadOptions(WebOptions options, Settings settings) {
        return loadOptions(options, settings, "hasor.config.web.");
    }

    /** 按指定前缀读取配置，供调用方兼容其自身的配置命名空间。 */
    public static WebOptions loadOptions(WebOptions options, Settings settings, String prefix) {
        Objects.requireNonNull(options, "options");
        if (settings == null) {
            return options;
        }
        options.staticResources(settings.getBoolean(prefix + "staticResources", options.isStaticResources()));
        String[] noStore = settings.getStringArray(prefix + "response.noStorePaths");
        if (noStore != null && noStore.length > 0) {
            options.noStorePaths(noStore);
        }
        String location = settings.getString(prefix + "staticLocation", null);
        if (StringUtils.isNotBlank(location)) {
            options.staticLocation(location);
        }
        String[] paths = settings.getStringArray(prefix + "spaPaths");
        if (paths != null && paths.length > 0) {
            options.spaPaths(paths);
        }
        String[] exclusions = settings.getStringArray(prefix + "resourceExcludes");
        if (exclusions != null && exclusions.length > 0) {
            options.resourceExcludes(exclusions);
        }

        String[] scanExcluded = settings.getStringArray(prefix + "scanExcludes");
        if (scanExcluded != null) {
            for (String name : scanExcluded) {
                if (name != null && !name.isBlank()) {
                    options.excludeScanNames(name);
                }
            }
        }
        return options;
    }

    @Override
    public void loadModule(WebApiBinder binder) throws Throwable {
        for (String path : this.config.getNoStorePaths()) {
            binder.filter(path).through(Integer.MIN_VALUE + 1, (invoker, chain) -> {
                if (!invoker.getHttpResponse().containsHeader("Cache-Control")) {
                    invoker.getHttpResponse().setHeader("Cache-Control", "no-store");
                }
                return chain.doNext(invoker);
            });
        }

        if (this.config.isStaticResources()) {
            ClassPathResourceLoader classpath = new ClassPathResourceLoader(binder.getClassLoader());
            PrefixResourceLoader loader = new PrefixResourceLoader(classpath, this.config.getStaticLocation());

            binder.addResource("/**", loader)                           //
                    .welcomeFile("index.html")                          //
                    .fallbackPaths(this.config.getSpaPaths())           //
                    .excludedPrefixes(this.config.getResourceExcludes())//
                    .order(Integer.MAX_VALUE);
        }
    }
}
