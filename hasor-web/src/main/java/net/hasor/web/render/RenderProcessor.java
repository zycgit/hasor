/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Produces;
import net.hasor.web.binder.RenderDef;
import net.hasor.web.render.none.NoopRenderEngine;

/**
 * Web 请求执行链中的内置渲染阶段，不参与业务过滤器注册和排序。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public final class RenderProcessor {
    private static final Logger                    logger        = LoggerFactory.getLogger(RenderProcessor.class);
    private              String                    layoutPath    = null;                    // 布局模版位置
    private              boolean                   useLayout     = true;
    private              String                    templatePath  = null;                    // 页面模版位置
    private final        Map<String, RenderEngine> engineMap     = new HashMap<>();
    private              String                    placeholder   = null;
    private              String                    defaultLayout = null;
    private              String                    defaultObjectEngine;
    private              String                    defaultStringEngine;

    public void doInit(AppContext appContext, String objectEngine, String stringEngine) throws Throwable {
        if (StringUtils.isBlank(objectEngine) || StringUtils.isBlank(stringEngine)) {
            throw new IllegalArgumentException("Default render names must not be blank");
        }
        this.defaultObjectEngine = objectEngine;
        this.defaultStringEngine = stringEngine;

        List<RenderDef> renderInfoList = appContext.findBindingBean(RenderDef.class);
        renderInfoList.sort(Comparator.comparing(RenderDef::isFallback));
        for (RenderDef renderInfo : renderInfoList) {
            if (renderInfo.isFallback() && this.engineMap.containsKey(renderInfo.getRenderName().toUpperCase(Locale.ROOT))) {
                continue;
            }

            String renderName = renderInfo.getRenderName();
            logger.info(String.format("web -> renderName %s.", renderName));
            this.engineMap.put(renderName.toUpperCase(Locale.ROOT), renderInfo.newEngine(appContext));
        }

        for (String name : new String[] { this.defaultObjectEngine, this.defaultStringEngine }) {
            if (!this.engineMap.containsKey(name.toUpperCase(Locale.ROOT))) {
                throw new IllegalStateException("Unknown default render engine: " + name);
            }

            RenderEngine engine = this.engineMap.get(name.toUpperCase(Locale.ROOT));
            if (engine instanceof net.hasor.web.render.json.AutoJsonRenderEngine automatic) {
                automatic.initialize();
            }
        }

        Settings settings = appContext.getSettings();
        this.useLayout = settings.getBoolean("hasor.layout.enable", true);
        this.layoutPath = settings.getString("hasor.layout.layoutPath", "/layout");
        this.templatePath = settings.getString("hasor.layout.templatePath", "/templates");
        this.placeholder = settings.getString("hasor.layout.placeholder", "content_placeholder");
        this.defaultLayout = settings.getString("hasor.layout.defaultLayout", "default.htm");
        logger.info(String.format("Render init -> useLayout=%s, layoutPath=%s, templatePath=%s, placeholder=%s, defaultLayout=%s",//
                this.useLayout, this.layoutPath, this.templatePath, this.placeholder, this.defaultLayout));
    }

    /** Renders the final result produced by MVC invocation and exception handling. */
    public Object invoke(Invoker invoker, Object returnData) throws Throwable {
        if (invoker instanceof RenderInvoker renderInvoker) {
            return this.doRenderInvoker(renderInvoker, returnData);
        } else {
            return returnData;
        }
    }

    private static RenderType findRenderType(Annotation[] annos) {
        return Arrays.stream(annos).map(a -> {
            if (a instanceof RenderType renderType) {
                return renderType;
            }
            return a.annotationType().getAnnotation(RenderType.class);
        }).filter((Predicate<Annotation>) Objects::nonNull).findFirst().orElse(null);
    }

    /** Initializes request defaults before the controller can override them. */
    public void initInvoker(Invoker dataContext) {
        if (!(dataContext instanceof RenderInvoker invoker)) {
            return;
        }

        // Layout 预设
        if (this.useLayout) {
            invoker.layoutEnable();
        } else {
            invoker.layoutDisable();
        }

        // 处理 RenderType
        if (invoker.ownerMapping() != null) {
            Method method = invoker.ownerMapping().findMethod(invoker.getHttpRequest());
            RenderType renderType = findRenderType(method);
            if (renderType != null && StringUtils.isNotBlank(renderType.value())) {
                invoker.renderType(renderType.value());
                String mimeType = invoker.getMimeType(renderType.value());
                if (StringUtils.isNotBlank(mimeType) && !hasProduces(method)) {
                    invoker.contentType(mimeType);
                }
            }
        }
    }

    private static RenderType findRenderType(Method method) {
        RenderType renderType = findRenderType(method.getAnnotations());
        return renderType != null ? renderType : findRenderType(method.getDeclaringClass().getAnnotations());
    }

    public Object doRenderInvoker(RenderInvoker invoker, Object returnData) throws Throwable {
        if (invoker.getHttpResponse().isCommitted()) {
            return returnData;
        }

        RenderEngine specialEngine = null;
        Method method = invoker.ownerMapping() == null ? null : invoker.ownerMapping().findMethod(invoker.getHttpRequest());
        if (method != null) {
            HttpServletResponse response = invoker.getHttpResponse();
            if ((response instanceof OwnedResponse owned && owned.isOwned()) || response.getStatus() == 204 || response.getStatus() == 304 || invoker.getHttpRequest().isAsyncStarted()) {
                return returnData;
            }
            RenderType renderType = findRenderType(method);
            if (renderType != null && renderType.engineType() != RenderType.DEFAULT.class) {
                specialEngine = invoker.getAppContext().getInstance(renderType.engineType());
            }

            // A view name keeps the existing template pipeline. Defaults apply only to mapped return values.
            if (StringUtils.isEmpty(invoker.renderTo())) {
                if (specialEngine == null && StringUtils.isEmpty(invoker.renderType())) {
                    if (returnData == null) {
                        return returnData;
                    }
                    invoker.renderType(returnData instanceof CharSequence ? this.defaultStringEngine : this.defaultObjectEngine);
                }

                RenderEngine engine = specialEngine != null ? specialEngine : this.engineMap.get(invoker.renderType());
                if (engine == null) {
                    throw new IllegalStateException("Unknown render engine: " + invoker.renderType());
                }
                if (engine instanceof NoopRenderEngine) {
                    return returnData;
                }
                if (engine instanceof RedirectTo.RedirectRenderEngine) {
                    engine.process(invoker, new StringWriter());
                    return returnData;
                }

                if (response.getContentType() == null) {
                    String type = "TEXT".equals(invoker.renderType()) ? "text/plain" : invoker.renderType() == null ? null : invoker.getMimeType(invoker.renderType().toLowerCase(Locale.ROOT));
                    response.setContentType(StringUtils.isBlank(type) ? "application/octet-stream" : type);
                }

                invoker.layoutDisable();
                StringWriter writer = new StringWriter();
                engine.process(invoker, writer);
                writeBody(writer.toString(), invoker);
                return returnData;
            }
        }

        // .处理渲染
        if (this.process(invoker, specialEngine)) {
            return returnData;
        }

        // .如果处理渲染失败，但是isCommitted = false，那么做服务端转发 renderTo
        HttpServletRequest httpRequest = invoker.getHttpRequest();
        HttpServletResponse httpResponse = invoker.getHttpResponse();
        if (!httpResponse.isCommitted() && !StringUtils.isEmpty(invoker.renderTo())) {
            RequestDispatcher requestDispatcher = httpRequest.getRequestDispatcher(invoker.renderTo());
            if (requestDispatcher != null) {
                requestDispatcher.forward(httpRequest, httpResponse);
            }
        }

        return returnData;
    }

    public boolean process(RenderInvoker render, RenderEngine engine) throws Throwable {
        if (engine == null) {
            String renderType = render.renderType();
            engine = this.engineMap.get(renderType);
            if (engine == null) {
                return false;
            }
        }

        if (engine instanceof NoopRenderEngine) {
            return true;
        }

        String oriViewName = render.renderTo();
        String newViewName = render.renderTo();
        if (render.layout()) {
            newViewName = this.templatePath + ((oriViewName.charAt(0) != '/') ? "/" : "") + oriViewName;
        }

        String layoutFile = null;
        if (render.layout()) {
            layoutFile = findLayout(engine, oriViewName);
        }

        StringWriter finalWriter = new StringWriter();
        if (layoutFile != null) {
            //先执行目标页面,然后在渲染layout
            StringWriter tmpWriter = new StringWriter();
            if (engine.exist(newViewName)) {
                render.renderTo(newViewName);
                engine.process(render, tmpWriter);
            } else {
                return false;
            }
            //渲染layout
            render.put(this.placeholder, tmpWriter.toString());
            if (engine.exist(layoutFile)) {
                render.renderTo(layoutFile);
                engine.process(render, finalWriter);
                return writerToResponse(finalWriter.toString(), render.getHttpResponse());
            } else {
                throw new IOException("layout '" + layoutFile + "' file is missing.");//不可能发生这个错误。
            }
        } else {
            if (engine.exist(newViewName)) {
                render.renderTo(newViewName);
                engine.process(render, finalWriter);
                return writerToResponse(finalWriter.toString(), render.getHttpResponse());
            } else {
                return false;//没有执行模版
            }
        }
    }

    private static boolean hasProduces(Method method) {
        return method.isAnnotationPresent(Produces.class) || method.getDeclaringClass().isAnnotationPresent(net.hasor.web.annotation.Produces.class);
    }

    private static void writeBody(String body, RenderInvoker invoker) throws IOException {
        HttpServletResponse response = invoker.getHttpResponse();
        byte[] bytes = body.getBytes(java.nio.charset.Charset.forName(response.getCharacterEncoding()));
        response.setContentLength(bytes.length);
        if (!"HEAD".equalsIgnoreCase(invoker.getHttpRequest().getMethod())) {
            response.getOutputStream().write(bytes);
        }
    }

    private boolean writerToResponse(String toString, HttpServletResponse httpResponse) throws IOException {
        byte[] finalBytes = toString.getBytes();
        httpResponse.setContentLength(finalBytes.length);
        ServletOutputStream outputStream = httpResponse.getOutputStream();
        outputStream.write(finalBytes);
        outputStream.flush();
        outputStream.close();
        return true;
    }

    private String findLayout(RenderEngine engine, String tempFile) throws IOException {
        File layoutFile = new File(this.layoutPath, tempFile);
        if (engine.exist(layoutFile.getPath())) {
            return layoutFile.getPath();
        } else {
            layoutFile = new File(layoutFile.getParent(), this.defaultLayout);
            if (engine.exist(layoutFile.getPath())) {
                return layoutFile.getPath();
            } else {
                while (layoutFile.getPath().startsWith(this.layoutPath)) {
                    layoutFile = new File(layoutFile.getParentFile().getParent(), this.defaultLayout);
                    if (engine.exist(layoutFile.getPath())) {
                        return layoutFile.getPath();
                    }
                }
            }
        }
        return null;
    }
}
