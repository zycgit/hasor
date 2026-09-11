/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web.cors;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.WebApiBinder;
import net.hasor.web.annotation.HttpMethod;

/** Configures one cross-origin request mapping. */
public class CorsRegistration {
    private final String      pathPattern;
    private       Set<String> allowedOrigins = new LinkedHashSet<>(Set.of("*"));
    private       Set<String> allowedMethods = new LinkedHashSet<>(Set.of("*"));
    private       Set<String> allowedHeaders = new LinkedHashSet<>(Set.of("content-type"));
    private       boolean     allowCredentials;
    private       long        maxAge         = 3600;
    private       int         order;

    CorsRegistration(String pathPattern) {
        if (StringUtils.isBlank(pathPattern)) {
            throw new IllegalArgumentException("pathPattern is blank.");
        }
        this.pathPattern = normalizePattern(pathPattern);
    }

    public CorsRegistration allowedOrigins(String... origins) {
        this.allowedOrigins = cleanValues(origins, "origins");
        return this;
    }

    public CorsRegistration allowedMethods(String... methods) {
        this.allowedMethods = cleanValues(methods, "methods");
        return this;
    }

    public CorsRegistration allowedHeaders(String... headers) {
        this.allowedHeaders = cleanValues(headers, "headers");
        return this;
    }

    public CorsRegistration allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public CorsRegistration maxAge(long maxAge) {
        if (maxAge < 0) {
            throw new IllegalArgumentException("maxAge must not be negative.");
        }
        this.maxAge = maxAge;
        return this;
    }

    public CorsRegistration setOrder(int order) {
        this.order = order;
        return this;
    }

    void register(WebApiBinder webBinder) {
        webBinder.filter(this.pathPattern).through(this.order, new ConfiguredCorsFilter(this));
    }

    private static Set<String> cleanValues(String[] values, String name) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        Set<String> cleaned = Arrays.stream(values).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new));
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        return cleaned;
    }

    private static String normalizePattern(String pathPattern) {
        String normalized = pathPattern.trim();
        if (normalized.endsWith("/**")) {
            return normalized.substring(0, normalized.length() - 2) + "*";
        }
        return normalized;
    }

    private static class ConfiguredCorsFilter extends CorsFilter {
        private final CorsRegistration config;

        ConfiguredCorsFilter(CorsRegistration config) {
            this.config = config;
        }

        @Override
        public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
            String httpMethod = invoker.getHttpRequest().getMethod();
            if (invoker.ownerMapping() != null && invoker.ownerMapping().findMethod(HttpMethod.OPTIONS) != null) {
                return chain.doNext(invoker);
            }

            HttpServletRequest request = invoker.getHttpRequest();
            HttpServletResponse response = invoker.getHttpResponse();
            String origin = request.getHeader("Origin");
            if (StringUtils.isNotBlank(origin)) {
                if (!this.config.allowedOrigins.contains("*") && !this.config.allowedOrigins.contains(origin)) {
                    return chain.doNext(invoker);
                }
                response.setHeader("Access-Control-Allow-Origin", this.config.allowedOrigins.contains("*") && !this.config.allowCredentials ? "*" : origin);
                if (this.config.allowCredentials) {
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                }
            } else if (this.config.allowedOrigins.contains("*")) {
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
            response.setHeader("Access-Control-Allow-Methods", String.join(",", this.config.allowedMethods));
            response.setHeader("Access-Control-Allow-Headers", String.join(",", this.config.allowedHeaders));
            response.setHeader("Access-Control-Max-Age", Long.toString(this.config.maxAge));
            return HttpMethod.OPTIONS.equalsIgnoreCase(httpMethod) ? null : chain.doNext(invoker);
        }
    }
}
