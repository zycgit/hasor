/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;

import java.util.Arrays;
import java.util.List;

import net.hasor.web.Invoker;
import net.hasor.web.binder.ResourceDef;
import net.hasor.web.render.OwnedResponse;

/** Static-resource dispatch when no Action matches, outside MVC interception and rendering. */
final class ResourceProcessor {
    private final List<ResourceHandler> handlers;

    public ResourceProcessor(ResourceDef[] definitions) {
        this.handlers = Arrays.stream(definitions).map(d -> {
            return new ResourceHandler(  //
                    d.loader(),          //
                    d.pathPattern(),     //
                    d.welcomeFile(),     //
                    d.fallbackPaths(),   //
                    d.excludedPrefixes(),//
                    d.cacheControl());
        }).toList();
    }

    public boolean handle(Invoker invoker) throws Throwable {
        if (invoker.ownerMapping() != null) {
            return false;
        }

        var res = invoker.getHttpResponse();
        boolean committedOrAsync = res.isCommitted() || invoker.getHttpRequest().isAsyncStarted();
        boolean responseOwned = !committedOrAsync && res instanceof OwnedResponse owned && owned.isOwned();
        if (committedOrAsync || responseOwned) {
            return true;
        }

        for (ResourceHandler h : this.handlers) {
            if (h.handle(invoker)) {
                return true;
            }
        }

        return false;
    }
}
