/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.FilenameUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.web.Invoker;

/**
 * 通过 ResourceLoader 来响应对于 Web 资源的请求。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-01
 */
final class ResourceHandler {
    private final ResourceLoader loader;
    private final String         urlPrefix;
    private final String         welcomeFile;
    private       Pattern[]      fallbackPaths    = new Pattern[0];
    private       String[]       excludedPrefixes = new String[0];
    private       String         cacheControl     = "no-cache";

    /** Runtime resource policy supplied by the binder at application startup. */
    public ResourceHandler(ResourceLoader loader, String urlPrefix, String welcomeFile, String[] fallbackPaths, String[] excludedPrefixes, String cacheControl) {
        this(loader, urlPrefix, welcomeFile);
        this.fallbackPaths(fallbackPaths);
        this.excludedPrefixes(excludedPrefixes);
        this.cacheControl(cacheControl);
    }

    private ResourceHandler(ResourceLoader loader, String urlPrefix, String welcomeFile) {
        this.loader = Objects.requireNonNull(loader, "resource loader is null.");
        this.urlPrefix = formatUrlPrefix(urlPrefix);
        this.welcomeFile = StringUtils.isBlank(welcomeFile) ? null : trimLeftSlash(welcomeFile.trim());
    }

    private static String formatUrlPrefix(String urlPrefix) {
        if (StringUtils.isBlank(urlPrefix)) {
            return null;
        }
        urlPrefix = urlPrefix.trim().replace('\\', '/').replaceAll("/+", "/");
        if (!urlPrefix.startsWith("/")) {
            urlPrefix = "/" + urlPrefix;
        }
        while (urlPrefix.length() > 1 && urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);
        }
        return urlPrefix;
    }

    private void fallbackPaths(String... paths) {
        this.fallbackPaths = Arrays.stream(paths).map(path -> {
            return Pattern.compile(Arrays.stream(path.split("\\*", -1)).map(Pattern::quote).collect(Collectors.joining(".*")));
        }).toArray(Pattern[]::new);
    }

    private void excludedPrefixes(String... prefixes) {
        this.excludedPrefixes = prefixes.clone();
    }

    private void cacheControl(String value) {
        this.cacheControl = value;
    }

    private static String trimLeftSlash(String path) {
        if (path == null) {
            return null;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private String resolveResource(String requestPath) {
        if (this.urlPrefix == null) {
            return requestPath;
        }
        if (requestPath.equals(this.urlPrefix) || requestPath.equals(this.urlPrefix + "/")) {
            return this.welcomeFile;
        }
        if ("/".equals(this.urlPrefix)) {
            return trimLeftSlash(requestPath);
        }
        if (!requestPath.startsWith(this.urlPrefix + "/")) {
            return null;
        }
        return trimLeftSlash(requestPath.substring(this.urlPrefix.length()));
    }

    private boolean matchesPath(String path) {
        if (this.urlPrefix != null && !"/".equals(this.urlPrefix) && !path.equals(this.urlPrefix) && !path.startsWith(this.urlPrefix + "/")) {
            return false;
        }
        for (String prefix : this.excludedPrefixes) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return false;
            }
        }
        return true;
    }

    boolean handle(Invoker invoker) throws Throwable {
        HttpServletRequest request = invoker.getHttpRequest();
        HttpServletResponse response = invoker.getHttpResponse();
        String path;
        try {
            path = URLDecoder.decode(invoker.getRequestPath().replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            if (!matchesPath(invoker.getRequestPath())) {
                return false;
            }
            response.sendError(400);
            return true;
        }

        if (!matchesPath(path)) {
            return false;
        }

        if (path.contains("\\") || path.contains("%") || path.contains(";") || path.chars().anyMatch(c -> c < 32) || path.matches(".*(?:^|/)\\.{1,2}(?:/|$).*")) {
            response.sendError(400);
            return true;
        }

        for (String prefix : this.excludedPrefixes) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return false;
            }
        }

        String resource = resolveResource(path);
        if (StringUtils.isBlank(resource)) {
            response.sendError(404);
            return true;
        }
        if (path.matches(".*(?:^|/)\\..*") || path.endsWith(".map") || resource.endsWith("/")) {
            response.sendError(404);
            return true;
        }
        if (this.welcomeFile != null && !this.loader.exist(resource) && FilenameUtils.getExtension(path).isEmpty()) {
            for (Pattern fallback : this.fallbackPaths) {
                if (fallback.matcher(path).matches()) {
                    resource = this.welcomeFile;
                    break;
                }
            }
        }
        if (StringUtils.isBlank(resource) || !this.loader.exist(resource)) {
            response.sendError(404);
            return true;
        }
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
            response.setHeader("Allow", "GET, HEAD");
            response.sendError(405);
            return true;
        }

        String mimeType = invoker.getMimeType(FilenameUtils.getExtension(resource));
        response.setContentType(StringUtils.isBlank(mimeType) ? "application/octet-stream" : mimeType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", this.cacheControl);
        URL url = this.loader.getResource(resource);
        long modified = 0;
        if (url != null) {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            modified = connection.getLastModified();
        }
        if (modified > 0) {
            response.setDateHeader("Last-Modified", modified);
            long since = -1;
            try {
                since = request.getDateHeader("If-Modified-Since");
            } catch (IllegalArgumentException ignored) {
                // Invalid conditional dates are ignored, as for a normal unconditional request.
            }
            if (since >= modified / 1000 * 1000) {
                response.setStatus(304);
                return true;
            }
        }

        long size = this.loader.getResourceSize(resource);
        if (size >= 0) {
            response.setContentLengthLong(size);
        }
        if ("HEAD".equals(request.getMethod())) {
            return true;
        }
        try (InputStream input = this.loader.getResourceAsStream(resource)) {
            IOUtils.copy(input, response.getOutputStream());
        }
        return true;
    }
}
