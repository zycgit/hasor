/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.health;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.hasor.core.BindInfo;
import net.hasor.cobble.setting.Settings;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

/** 健康检查默认装配，配置归属于 Boot Web。 */
public final class HealthModule implements WebModule {
    @Override
    public void loadModule(WebApiBinder binder) {
        Settings settings = binder.getSettings();
        if (!settings.getBoolean("hasor.boot.web.health.enabled", true)) {
            return;
        }

        String path = settings.getString("hasor.boot.web.health.path", "/health");
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.contains("..") || path.contains("*") || path.contains("{") || path.contains("}") || path.contains("?") || path.contains("#") || path.contains("\\") || path.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Health path must be an absolute literal URL path.");
        }
        // 保留已注册的业务接口，不覆盖其行为。
        if (binder.getMappings().stream().anyMatch(mapping -> path.equals(mapping.getMappingTo()))) {
            return;
        }
        HealthEndpoint endpoint = new HealthEndpoint();
        binder.mappingTo(path).with(Integer.MAX_VALUE, endpoint);
        binder.lazyLoad(context -> {
            Map<String, Supplier<? extends HealthCheck>> checks = new LinkedHashMap<>();
            for (BindInfo<HealthCheck> binding : context.findBindingRegister(HealthCheck.class)) {
                String name = binding.getBindName();
                if (name == null || name.isBlank()) {
                    name = binding.getBindID();
                }
                if (name == null || name.isBlank() || checks.containsKey(name)) {
                    throw new IllegalArgumentException("Health check bean names must be nonblank and unique: " + name);
                }
                checks.put(name, context.getProvider(binding));
            }
            endpoint.initialize(context, checks);
        });
    }
}
