/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.invoker;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.core.spi.SpiTrigger;
import net.hasor.web.*;
import net.hasor.web.binder.*;
import net.hasor.web.render.OwnedResponse;
import net.hasor.web.render.RenderProcessor;
import net.hasor.web.spi.MappingDiscoverer;

/**
 * 上下文。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class InvokerContext {
    protected static Logger               logger            = LoggerFactory.getLogger(InvokerContext.class);
    private          AppContext           appContext        = null;
    private          Mapping[]            invokeArray       = new Mapping[0];
    private          FilterDef[]          filters           = new FilterDef[0];
    private          ExceptionDef<?>[]    exceptionHandlers = new ExceptionDef<?>[0];
    private          HandlerInterceptor[] interceptors      = new HandlerInterceptor[0];
    private          RootInvokerCreater   invokerCreator    = null;
    private          RenderProcessor      renderProcessor;
    private          ResourceProcessor    resourceProcessor;
    private          ServletVersion       servletVersion;

    public void initContext(final AppContext appContext, final OneConfig configMap) throws Throwable {
        this.appContext = Objects.requireNonNull(appContext);
        this.exceptionHandlers = appContext.getInstance(ExceptionDef[].class);
        this.interceptors = appContext.getInstance(HandlerInterceptor[].class);
        this.renderProcessor = appContext.getInstance(RenderProcessor.class);
        this.resourceProcessor = new ResourceProcessor(appContext.getInstance(ResourceDef[].class));
        this.servletVersion = appContext.getInstance(ServletVersion.class);

        // .MappingData
        List<MappingDef> mappingList = appContext.findBindingBean(MappingDef.class);
        mappingList.sort(Comparator.comparingLong(MappingDef::getIndex));
        this.invokeArray = mappingList.toArray(new Mapping[0]);
        for (Mapping inMapping : this.invokeArray) {
            logger.info(String.format("mapingTo -> type '%s' mappingTo: '%s'.", inMapping.getTargetType().getBindType(), inMapping.getMappingTo()));
        }

        // .discover
        SpiTrigger spiTrigger = appContext.getInstance(SpiTrigger.class);
        for (Mapping mapping : invokeArray) {
            spiTrigger.notifySpiWithoutResult(MappingDiscoverer.class, listener -> {
                listener.discover(mapping);
            });
        }

        // .Filters
        this.filters = appContext.findBindingBean(FilterDef.class).stream()//
                .sorted(Comparator.comparingLong(FilterDef::getIndex))     //
                .toArray(FilterDef[]::new);                                //

        // .init
        for (FilterDef filter : this.filters) {
            filter.init(configMap);
        }

        // .creator
        this.invokerCreator = new RootInvokerCreater(appContext);
    }

    public void destroyContext() {
        for (InvokerFilter filter : this.filters) {
            filter.destroy();
        }
    }

    public Invoker newInvoker(Mapping define, HttpServletRequest request, HttpServletResponse response) {
        if (!(response instanceof OwnedResponse)) {
            response = new OwnedResponse(response);
        }

        return this.invokerCreator.createExt(new InvokerSupplier(define, this.appContext, request, response));
    }

    public ExecuteCaller genCaller(HttpServletRequest httpReq, HttpServletResponse httpRes) {
        Mapping foundDefine = null;
        for (Mapping define : this.invokeArray) {
            if (define.matchingMapping(httpReq)) {
                foundDefine = define;
                break;
            }
        }

        Invoker invoker = this.newInvoker(foundDefine, httpReq, httpRes);
        ExecuteCaller ec = null;
        if (foundDefine == null) {
            ec = (chain) -> {
                BasicFuture<Object> future = new BasicFuture<>();
                try {
                    if (!this.resourceProcessor.handle(invoker) && chain != null) {
                        chain.doFilter(invoker.getHttpRequest(), invoker.getHttpResponse());
                    }
                    future.completed(null);
                } catch (Throwable e) {
                    future.failed(e);
                }
                return future;
            };
        } else {
            ec = new InvokerCaller(() -> {
                return invoker;
            }, this.filters, this.interceptors, this.exceptionHandlers, this.renderProcessor, this.servletVersion);
        }

        ExecuteCaller finalEC = ec;
        return chain -> HttpParameters.executeWorker(invoker, () -> {
            return finalEC.invoke(chain);
        });
    }
}
