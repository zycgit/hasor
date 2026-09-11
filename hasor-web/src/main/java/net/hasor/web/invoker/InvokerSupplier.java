/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.function.EFunction;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.web.Invoker;
import net.hasor.web.Mapping;
import net.hasor.web.MimeType;

/**
 * {@link Invoker} 接口实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class InvokerSupplier implements Invoker {
    protected static Logger              logger          = LoggerFactory.getLogger(InvokerSupplier.class);
    private final    Set<String>         lockKeys        = new HashSet<>();
    private          HttpServletRequest  httpRequest     = null;
    private          HttpServletResponse httpResponse    = null;
    private          AppContext          appContext      = null;
    private          String              contentType     = null;    // 内容类型（如果指定了内容类型，那么会设置setContentType）
    private          MimeType            mimeType        = null;
    private          Mapping             ownerInMapping  = null;
    private          boolean             jsonBodyBoolean = false;
    private          String              jsonBody        = null;

    protected InvokerSupplier(Mapping ownerInMapping, AppContext appContext, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        //
        this.ownerInMapping = ownerInMapping;
        this.appContext = appContext;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.mimeType = appContext.getInstance(MimeType.class);
        //
        this.put(ROOT_DATA_KEY, this);
        this.put(REQUEST_KEY, this.httpRequest);
        this.put(RESPONSE_KEY, this.httpResponse);
        //
        this.lockKey(ROOT_DATA_KEY);// rootData
        this.lockKey(REQUEST_KEY);  // response
        this.lockKey(RESPONSE_KEY); // request
    }

    @Override
    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public HttpServletRequest getHttpRequest() {
        return this.httpRequest;
    }

    @Override
    public HttpServletResponse getHttpResponse() {
        return this.httpResponse;
    }

    @Override
    public <T> Future<T> asyncExecute(EFunction<Invoker, T, Throwable> consumer, Executor executor) {
        BasicFuture<T> future = new BasicFuture<>();
        Invoker invoker = this;
        executor.execute(() -> {
            try {
                T result = HttpParameters.executeWorker(invoker, () -> {
                    return consumer.eApply(invoker);
                });
                future.completed(result);
            } catch (Throwable e) {
                future.failed(e);
            }
        });
        return future;
    }

    @Override
    public Mapping ownerMapping() {
        return this.ownerInMapping;
    }

    @Override
    public String getJsonBodyString() {
        if (this.jsonBodyBoolean) {
            return this.jsonBody;
        }
        HttpServletRequest httpRequest = getHttpRequest();
        if (httpRequest.getContentType() != null && httpRequest.getContentType().contains("application/json")) {
            try (Reader reader = httpRequest.getReader()) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(reader, writer);
                this.jsonBody = writer.toString();
                this.jsonBodyBoolean = true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return this.jsonBody;
    }

    @Override
    public String contentType() {
        if (StringUtils.isNotBlank(this.contentType)) {
            return this.contentType;
        } else {
            String contentType = ownerMapping().getSpecialContentType(getHttpRequest().getMethod());
            if (StringUtils.isBlank(contentType)) {
                String viewName = getRequestPath();
                int lastIndex = viewName.lastIndexOf(".");
                if (lastIndex > 0) {
                    contentType = getMimeType(viewName.substring(lastIndex + 1));
                }
            }
            return contentType;
        }
    }

    @Override
    public void contentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean isLockKey(String key) {
        return this.lockKeys.contains(key);
    }

    @Override
    public void lockKey(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        this.lockKeys.add(key);
    }

    @Override
    public String getMimeType(String suffix) {
        return this.mimeType.getMimeType(suffix);
    }

    @Override
    public <T> T fillForm(Class<? extends T> formType, T bean) {
        return new InvokerCallerParamsBuilder().getParamsParam(this, formType, bean);
    }
}
