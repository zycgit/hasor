package net.hasor.web.binder;

import net.hasor.cobble.loader.ResourceLoader;

/**
 * Internal configuration snapshot passed from binder assembly to the invoker runtime.
 * Public only for cross-package access; applications configure resources through ResourceBinder.
 * Contains no request processing or handler construction logic.
 */
public record ResourceDef(ResourceLoader loader, String pathPattern, String welcomeFile, String[] fallbackPaths, String[] excludedPrefixes, String cacheControl) {
    public ResourceDef {
        fallbackPaths = fallbackPaths.clone();
        excludedPrefixes = excludedPrefixes.clone();
    }

    @Override
    public String[] fallbackPaths() {
        return this.fallbackPaths.clone();
    }

    @Override
    public String[] excludedPrefixes() {
        return this.excludedPrefixes.clone();
    }
}
