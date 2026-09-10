package net.hasor.boot.web;

import javax.servlet.ServletContext;
import net.hasor.config.web.WebOptions;
import net.hasor.core.Module;
import net.hasor.web.startup.RuntimeListener;

/** Adds Boot conventions without changing standalone Hasor Web or custom context factories. */
public class BootRuntimeListener extends RuntimeListener {
    @Override
    protected Module newRootModule(ServletContext context, String rootModule) throws Exception {
        Module application = null;
        if (rootModule != null && !rootModule.isBlank()) {
            Class<?> source = Thread.currentThread().getContextClassLoader().loadClass(rootModule);
            if (Module.class.isAssignableFrom(source)) {
                application = super.newRootModule(context, rootModule);
            }
        }

        WebServerConfig config = (WebServerConfig) context.getAttribute(WebServerConfig.class.getName());
        if (config != null) {
            // 在 ConfigurationModule 装配之前提供代码式 Web 选项，扫描仍由 Config 统一执行。
            context.setAttribute(WebOptions.class.getName(), config.getWebOptions());
        }
        return new BootWebModule(application, config);
    }
}