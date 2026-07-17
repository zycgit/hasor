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
package net.hasor.web.objects;
import java.io.InputStream;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.FilenameUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerFilter;

/**
 * 通过 ResourceLoader 来响应对于 Web 资源的请求。
 * @version : 2020-03-01
 * @author 赵永春 (zyc@hasor.net)
 */
public class ResourceFilter implements InvokerFilter {
    private final ResourceLoader loader;
    private final String         urlPrefix;
    private final String         welcomeFile;

    public ResourceFilter(ResourceLoader loader) {
        this(loader, null, null);
    }

    public ResourceFilter(ResourceLoader loader, String urlPrefix) {
        this(loader, urlPrefix, "index.html");
    }

    public ResourceFilter(ResourceLoader loader, String urlPrefix, String welcomeFile) {
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
        if (!requestPath.startsWith(this.urlPrefix + "/")) {
            return null;
        }
        return trimLeftSlash(requestPath.substring(this.urlPrefix.length()));
    }

    @Override
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        String resource = resolveResource(invoker.getRequestPath());
        if (StringUtils.isBlank(resource) || !this.loader.exist(resource)) {
            return chain.doNext(invoker);
        }
        //
        HttpServletResponse httpResponse = invoker.getHttpResponse();
        String extension = FilenameUtils.getExtension(resource);
        String mimeType = invoker.getMimeType(extension);
        if (StringUtils.isNotBlank(mimeType)) {
            httpResponse.setContentType(mimeType);
        }
        //
        long size = this.loader.getResourceSize(resource);
        if (size > 0) {
            if (size >= Integer.MAX_VALUE) {
                httpResponse.setContentLengthLong(size);
            } else {
                httpResponse.setContentLength((int) size);
            }
        }
        //
        try (ServletOutputStream outputStream = httpResponse.getOutputStream()) {
            try (InputStream inputStream = loader.getResourceAsStream(resource)) {
                IOUtils.copy(inputStream, outputStream);
            }
        }
        return null;
    }
}
