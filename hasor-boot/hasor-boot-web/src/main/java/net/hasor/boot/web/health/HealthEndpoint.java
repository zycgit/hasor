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
import net.hasor.core.AppContext;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Get;

/** 内置健康检查响应，不输出检查异常或敏感连接信息。 */
public final class HealthEndpoint {
    private AppContext               context;
    private Map<String, Supplier<? extends HealthCheck>> checks = Map.of();

    void initialize(AppContext context, Map<String, Supplier<? extends HealthCheck>> checks) {
        this.context = context;
        this.checks = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(checks));
    }

    @Get
    public Map<String, Object> health(Invoker invoker) {
        boolean healthy = context != null && context.isStart();
        Map<String, String> components = new LinkedHashMap<>();
        for (Map.Entry<String, Supplier<? extends HealthCheck>> entry : checks.entrySet()) {
            boolean available;
            try {
                available = entry.getValue().get().check();
            } catch (Exception ignored) {
                available = false;
            }
            components.put(entry.getKey(), available ? "UP" : "DOWN");
            healthy &= available;
        }

        invoker.getHttpResponse().setStatus(healthy ? 200 : 503);
        invoker.getHttpResponse().setHeader("Cache-Control", "no-store");
        return components.isEmpty() ? Map.of("status", healthy ? "UP" : "DOWN") : Map.of("status", healthy ? "UP" : "DOWN", "checks", components);
    }
}
