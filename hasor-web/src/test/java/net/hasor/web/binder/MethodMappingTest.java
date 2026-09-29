/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.List;
import java.util.Set;
import net.hasor.core.AppContext;
import net.hasor.test.web.actions.methodmapping.DuplicateAction;
import net.hasor.test.web.actions.methodmapping.MethodMappedAction;
import net.hasor.test.web.actions.methodmapping.PrefixedAction;
import net.hasor.test.web.actions.methodmapping.StaticAction;
import net.hasor.web.AbstractTest;
import org.junit.Test;
import static org.junit.Assert.*;

public class MethodMappingTest extends AbstractTest {
    @Test
    public void methodPathsSelectTheirOwnHttpMethodsAndBindParameters() throws Throwable {
        try (AppContext app = this.buildWebAppContext(binder -> {
            binder.loadMappingTo(MethodMappedAction.class);
        }, this.servlet30("/"), LoadModule.Web)) {
            List<MappingDef> mappings = app.findBindingBean(MappingDef.class);
            assertEquals(5, mappings.size());
            MappingDef items = mappings.stream().filter(mapping -> {
                return mapping.getMappingTo().equals("/items/{id}");
            }).findFirst().orElseThrow();

            assertEquals("read", items.findMethod("GET").getName());
            assertEquals("create", items.findMethod("POST").getName());
            assertNull(items.findMethod("DELETE"));
            assertEquals("read:42", this.mockAndCallHttp("GET", "http://localhost/items/42", app));
            assertEquals("create:7", this.mockAndCallHttp("POST", "http://localhost/items/7", app));
            assertEquals("hello", this.mockAndCallHttp("PATCH", "http://localhost/echo?value=hello", app));
            assertEquals("pong", this.mockAndCallHttp("GET", "http://localhost/alias", app));
            assertEquals("get", this.mockAndCallHttp("GET", "http://localhost/fallback", app));
            assertEquals("fallback", this.mockAndCallHttp("DELETE", "http://localhost/fallback", app));
        }
    }

    @Test
    public void classPrefixesCombineWithMethodPathsAndAliases() throws Throwable {
        try (AppContext app = this.buildWebAppContext(binder -> {
            binder.loadMappingTo(Set.of(PrefixedAction.class));
        }, this.servlet30("/"), LoadModule.Web)) {
            List<String> paths = app.findBindingBean(MappingDef.class).stream().map(MappingDef::getMappingTo).sorted().toList();
            assertEquals(List.of("/v1/", "/v1/health", "/v2/", "/v2/health"), paths);
            assertEquals("healthy", this.mockAndCallHttp("GET", "http://localhost/v2/health", app));
        }
    }

    @Test
    public void setRegistrationAlsoAcceptsControllersWithOnlyMethodAnnotations() {
        try (AppContext app = this.buildWebAppContext(binder -> {
            binder.loadMappingTo(Set.of(MethodMappedAction.class));
        }, this.servlet30("/"), LoadModule.Web)) {
            assertEquals(5, app.findBindingBean(MappingDef.class).size());
        }
    }

    @Test
    public void duplicateHttpMethodsAndStaticActionsFailDuringRegistration() {
        try (AppContext ignored = this.buildWebAppContext(binder -> {
            try {
                binder.loadMappingTo(DuplicateAction.class);
                fail("Duplicate HTTP mappings must fail");
            } catch (IllegalStateException duplicate) {
                assertTrue(duplicate.getMessage().contains("Conflicting mapping GET /duplicate"));
            }

            try {
                binder.loadMappingTo(StaticAction.class);
                fail("Static controller methods must fail");
            } catch (IllegalStateException invalid) {
                assertTrue(invalid.getMessage().contains("public instance method"));
            }
        }, this.servlet30("/"), LoadModule.Web)) {
        }
    }
}