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
package net.hasor.boot.web;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.ServletContext;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.core.Module;
import net.hasor.web.startup.RuntimeListener;

/**
 * Configuration shared by embedded servlet containers.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public class WebServerConfig {
    public static final String                               HASOR_ROOT_MODULE  = "hasor-root-module";
    public static final String                               HASOR_HCONFIG_FILE = "hasor-hconfig-file";
    public static final String                               HASOR_HCONFIG_NAME = "hasor-hconfig-name";
    public static final String                               HASOR_MAIN_ARGS    = RuntimeListener.HASOR_MAIN_ARGS;
    private             String                               host               = "0.0.0.0";
    private             int                                  port               = 8080;
    private             String                               contextPath        = "/";
    private             String                               filterName         = "hasorFilter";
    private             String                               filterPattern      = "/*";
    private             File                                 documentRoot       = defaultDocumentRoot();
    private             Class<? extends Module>              rootModule;
    private             String                               hconfigFile;
    private             String                               server;
    private             String[]                             arguments          = new String[0];
    private             Function<ServletContext, AppContext> appContextFactory;
    private final       Map<String, String>                  initParameters     = new LinkedHashMap<>();

    public WebServerConfig() {
    }

    public WebServerConfig(WebServerConfig source) {
        if (source == null) {
            return;
        }
        this.host = source.host;
        this.port = source.port;
        this.contextPath = source.contextPath;
        this.filterName = source.filterName;
        this.filterPattern = source.filterPattern;
        this.documentRoot = source.documentRoot;
        this.rootModule = source.rootModule;
        this.hconfigFile = source.hconfigFile;
        this.server = source.server;
        this.arguments = source.arguments == null ? null : source.arguments.clone();
        this.appContextFactory = source.appContextFactory;
        this.initParameters.putAll(source.initParameters);
    }

    public static WebServerConfig of(Class<? extends Module> rootModule) {
        return new WebServerConfig().rootModule(rootModule);
    }

    public static WebServerConfig of(Settings settings, Class<? extends Module> rootModule) {
        return of(rootModule).loadSettings(settings);
    }

    private static File defaultDocumentRoot() {
        File webapp = new File("src/main/webapp");
        if (webapp.isDirectory()) {
            return webapp;
        }
        return new File(System.getProperty("java.io.tmpdir"), "hasor-webroot");
    }

    public String getHost() {
        return this.host;
    }

    public WebServerConfig loadSettings(Settings settings) {
        if (settings == null) {
            return this;
        }
        String server = settings.getString("hasor.http.server", null);
        if (StringUtils.isNotBlank(server)) {
            this.server(server);
        }
        String host = settings.getString("hasor.http.host", null);
        if (StringUtils.isNotBlank(host)) {
            this.host(host);
        }
        Integer port = settings.getInteger("hasor.http.port", null);
        if (port != null) {
            this.port(port);
        }
        String contextPath = settings.getString("hasor.http.contextPath", null);
        if (StringUtils.isNotBlank(contextPath)) {
            this.contextPath(contextPath);
        }
        return this;
    }

    public WebServerConfig host(String host) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("host is blank.");
        }
        this.host = host.trim();
        return this;
    }

    public int getPort() {
        return this.port;
    }

    public WebServerConfig port(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535.");
        }
        this.port = port;
        return this;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public WebServerConfig contextPath(String contextPath) {
        this.contextPath = normalizeContextPath(contextPath);
        return this;
    }

    public String getFilterName() {
        return this.filterName;
    }

    public WebServerConfig filterName(String filterName) {
        if (StringUtils.isBlank(filterName)) {
            throw new IllegalArgumentException("filterName is blank.");
        }
        this.filterName = filterName.trim();
        return this;
    }

    public String getFilterPattern() {
        return this.filterPattern;
    }

    public WebServerConfig filterPattern(String filterPattern) {
        if (StringUtils.isBlank(filterPattern)) {
            throw new IllegalArgumentException("filterPattern is blank.");
        }
        this.filterPattern = filterPattern.trim();
        return this;
    }

    public File getDocumentRoot() {
        return this.documentRoot;
    }

    public WebServerConfig documentRoot(File documentRoot) {
        if (documentRoot == null) {
            throw new IllegalArgumentException("documentRoot is null.");
        }
        this.documentRoot = documentRoot;
        return this;
    }

    public Class<? extends Module> getRootModule() {
        return this.rootModule;
    }

    public WebServerConfig rootModule(Class<? extends Module> rootModule) {
        if (rootModule == null) {
            throw new IllegalArgumentException("rootModule is null.");
        }
        this.rootModule = rootModule;
        return this;
    }

    public String getHconfigFile() {
        return this.hconfigFile;
    }

    public WebServerConfig hconfigFile(String hconfigFile) {
        this.hconfigFile = hconfigFile;
        return this;
    }

    public String getServer() {
        return this.server;
    }

    public WebServerConfig server(String server) {
        this.server = StringUtils.isBlank(server) ? null : server.trim();
        return this;
    }

    public String[] getArguments() {
        return this.arguments == null ? null : this.arguments.clone();
    }

    public WebServerConfig arguments(String... arguments) {
        this.arguments = arguments == null ? new String[0] : arguments.clone();
        return this;
    }

    public Function<ServletContext, AppContext> getAppContextFactory() {
        return this.appContextFactory;
    }

    public WebServerConfig appContextFactory(Function<ServletContext, AppContext> appContextFactory) {
        this.appContextFactory = appContextFactory;
        return this;
    }

    public WebServerConfig initParameter(String name, String value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("init parameter name is blank.");
        }
        if (value == null) {
            this.initParameters.remove(name);
        } else {
            this.initParameters.put(name, value);
        }
        return this;
    }

    public Map<String, String> getInitParameters() {
        Map<String, String> initParams = new LinkedHashMap<>(this.initParameters);
        if (this.rootModule != null) {
            initParams.putIfAbsent(HASOR_ROOT_MODULE, this.rootModule.getName());
        }
        if (StringUtils.isNotBlank(this.hconfigFile)) {
            initParams.putIfAbsent(HASOR_HCONFIG_FILE, this.hconfigFile);
            initParams.putIfAbsent(HASOR_HCONFIG_NAME, this.hconfigFile);
        }
        return Collections.unmodifiableMap(initParams);
    }

    public Map<String, Object> getServletContextAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(HASOR_MAIN_ARGS, getArguments());
        return Collections.unmodifiableMap(attributes);
    }

    public WebServerConfig copy() {
        return new WebServerConfig(this);
    }

    public static String normalizeContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath) || "/".equals(contextPath.trim())) {
            return "/";
        }
        String normalized = contextPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
