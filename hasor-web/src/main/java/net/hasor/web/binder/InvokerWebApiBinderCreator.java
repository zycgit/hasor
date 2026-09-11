/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.io.IOException;
import javax.servlet.ServletContext;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.ApiBinder;
import net.hasor.core.binder.ApiBinderCreator;
import net.hasor.web.MimeType;
import net.hasor.web.ServletVersion;
import net.hasor.web.WebApiBinder;
import net.hasor.web.mime.MimeTypeSupplier;

/**
 * Web 基础设施的 ApiBinder 扩展器。
 * 让 {@link ApiBinder} 支持 {@link WebApiBinder} 类型
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class InvokerWebApiBinderCreator implements ApiBinderCreator<WebApiBinder> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public WebApiBinder createBinder(final ApiBinder apiBinder) throws IOException {
        try {
            apiBinder.getClassLoader().loadClass("javax.servlet.ServletContext");
        } catch (ClassNotFoundException e) {
            return null;
        }
        if (!(apiBinder.getContext() instanceof ServletContext)) {
            return null;
        }
        return newBinder(apiBinder);
    }

    //
    public static WebApiBinder newBinder(ApiBinder apiBinder) throws IOException {
        Object context = apiBinder.getContext();
        if (!(context instanceof ServletContext servletContext)) {
            return null;
        }
        //
        // .MimeType
        MimeTypeSupplier mimeTypeContext = new MimeTypeSupplier(servletContext);
        mimeTypeContext.loadResource("/META-INF/mime.types.xml");
        mimeTypeContext.loadResource("mime.types.xml");
        apiBinder.bindType(MimeType.class, mimeTypeContext);
        //
        //.ServletVersion
        ServletVersion curVersion = ServletVersion.V2_3;
        try {
            apiBinder.getClassLoader().loadClass("javax.servlet.ServletRequestListener");
            curVersion = ServletVersion.V2_4;
            servletContext.getContextPath();
            curVersion = ServletVersion.V2_5;
            servletContext.getEffectiveMajorVersion();
            curVersion = ServletVersion.V3_0;
            servletContext.getVirtualServerName();
            curVersion = ServletVersion.V3_1;
        } catch (Throwable e) {
            /* 忽略 */
        }
        //
        // .Binder
        apiBinder.bindType(ServletContext.class).toInstance(servletContext);
        apiBinder.bindType(ServletVersion.class).toInstance(curVersion);
        //
        InvokerWebApiBinder webBinder = new InvokerWebApiBinder(curVersion, mimeTypeContext, apiBinder);
        apiBinder.lazyLoad(appContext -> {
            try {
                webBinder.initialize(appContext);
            } catch (Throwable e) {
                throw new IllegalStateException("Cannot initialize Web request processing", e);
            }
        });
        return webBinder;
    }
}
