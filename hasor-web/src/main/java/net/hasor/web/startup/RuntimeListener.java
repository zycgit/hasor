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
package net.hasor.web.startup;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.servlet.*;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Module;
import net.hasor.core.spi.SpiTrigger;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class RuntimeListener implements ServletContextListener, HttpSessionListener, ServletRequestListener {
    protected Logger             logger                = LoggerFactory.getLogger(getClass());
    public static final String   AppContextName        = AppContext.class.getName();
    public static final String   AppContextFactoryName = AppContext.class.getName() + ".factory";
    public static final String   HASOR_MAIN_ARGS       = "hasor-main-args";
    private boolean              contextIsOutSite      = false;
    private Supplier<AppContext> appContext            = null;
    private SpiTrigger           spiTrigger            = null;

    /*----------------------------------------------------------------------------------------------------*/
    public RuntimeListener() {
        this.contextIsOutSite = false;
    }

    public RuntimeListener(AppContext appContext) {
        this(appContextSupplier(appContext));
    }

    public RuntimeListener(Supplier<AppContext> appContext) {
        this.appContext = Objects.requireNonNull(appContext, "appContext is null.");
        this.contextIsOutSite = true;
    }

    private static Supplier<AppContext> appContextSupplier(AppContext appContext) {
        Objects.requireNonNull(appContext, "appContext is null.");
        return () -> appContext;
    }

    /** 获取{@link AppContext} */
    public static AppContext getAppContext(ServletContext sc) {
        return (AppContext) sc.getAttribute(RuntimeListener.AppContextName);
    }
    /*----------------------------------------------------------------------------------------------------*/

    /** 创建{@link AppContext}对象 */
    protected Hasor newHasor(ServletContext sc, String configName) throws Throwable {
        Hasor h = Hasor.create(sc);
        //
        if (StringUtils.isNotBlank(configName)) {
            h.mainSettingWith(configName);
        }

        Object args = sc.getAttribute(HASOR_MAIN_ARGS);
        if (args instanceof String[] mainArgs) {
            h.bindArguments(mainArgs);
        }

        return h;
    }

    /** 获取启动模块 */
    protected Module newRootModule(ServletContext sc, String rootModule) throws Exception {
        if (StringUtils.isBlank(rootModule)) {
            logger.info("web initModule is undefinition.");
            return null;
        } else {
            Class<Module> startModuleClass = (Class<Module>) Thread.currentThread().getContextClassLoader().loadClass(rootModule);
            logger.info("web initModule is " + rootModule);
            return ClassUtils.newInstance(startModuleClass);
        }
    }

    protected AppContext doInit(ServletContext sc) {
        try {
            String rootModule = sc.getInitParameter("hasor-root-module");       // 启动入口
            String configName = sc.getInitParameter("hasor-hconfig-file");      // 配置文件名
            //
            Module startModule = this.newRootModule(sc, rootModule);
            //
            Hasor h = this.newHasor(sc, configName);
            String webContextDir = sc.getRealPath("/");
            System.setProperty("HASOR_WEBROOT", webContextDir);
            return h.build(startModule);
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void contextInitialized(final ServletContextEvent sce) {
        // 1. 初始化
        if (this.appContext == null) {
            ServletContext sc = sce.getServletContext();
            Object factory = sc.getAttribute(AppContextFactoryName);
            if (factory instanceof Function<?, ?>) {
                Function<ServletContext, AppContext> appContextFactory = (Function<ServletContext, AppContext>) factory;
                this.appContext = appContextSupplier(appContextFactory.apply(sc));
            } else {
                this.appContext = appContextSupplier(this.doInit(sc));
            }
        }

        this.spiTrigger = this.appContext.get().getInstance(SpiTrigger.class);

        // 2.放入ServletContext环境。
        logger.info("ServletContext Attribute is " + RuntimeListener.AppContextName);
        sce.getServletContext().setAttribute(RuntimeListener.AppContextName, this.appContext.get());
        //
        this.spiTrigger.notifySpiWithoutResult(ServletContextListener.class, l -> {
            l.contextInitialized(sce);
        });
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent se) {
        if (this.spiTrigger != null) {
            this.spiTrigger.notifySpiWithoutResult(ServletContextListener.class, l -> {
                l.contextDestroyed(se);
            });
        }

        if (!this.contextIsOutSite && this.appContext != null) {
            this.appContext.get().shutdown();
            this.logger.info("shutdown.");
        }
    }

    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        this.spiTrigger.notifySpiWithoutResult(HttpSessionListener.class, l -> {
            l.sessionCreated(se);
        });
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {
        this.spiTrigger.notifySpiWithoutResult(HttpSessionListener.class, l -> {
            l.sessionDestroyed(se);
        });
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        this.spiTrigger.notifySpiWithoutResult(ServletRequestListener.class, l -> {
            l.requestDestroyed(sre);
        });
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        this.spiTrigger.notifySpiWithoutResult(ServletRequestListener.class, l -> {
            l.requestInitialized(sre);
        });
    }
}
