/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web.cors;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.hasor.web.WebApiBinder;

/** Collects cross-origin request mappings. */
public class CorsRegistry {
    private final WebApiBinder           webBinder;
    private final List<CorsRegistration> registrations = new ArrayList<>();

    public CorsRegistry(WebApiBinder webBinder) {
        this.webBinder = Objects.requireNonNull(webBinder, "webBinder is null.");
    }

    public CorsRegistration addMapping(String pathPattern) {
        CorsRegistration registration = new CorsRegistration(pathPattern);
        this.registrations.add(registration);
        return registration;
    }

    public void register() {
        this.registrations.forEach(registration -> registration.register(this.webBinder));
    }
}
