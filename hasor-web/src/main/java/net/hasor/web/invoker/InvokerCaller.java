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
package net.hasor.web.invoker;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import javax.servlet.AsyncContext;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.web.*;
import net.hasor.web.binder.FilterDef;
import net.hasor.web.render.RenderProcessor;

/**
 * 负责解析参数并执行调用。
 * @version : 2014年8月27日
 * @author 赵永春 (zyc@hasor.net)
 */
class InvokerCaller extends InvokerCallerParamsBuilder implements ExecuteCaller {
    protected static Logger   logger          = LoggerFactory.getLogger(InvokerCaller.class);
    private FilterDef[]       filterArrays    = null;
    private Supplier<Invoker> invokerSupplier = null;

    private final RenderProcessor renderProcessor;
    private final ServletVersion  servletVersion;

    public InvokerCaller(Supplier<Invoker> invokerSupplier, FilterDef[] filterArrays, RenderProcessor renderProcessor, ServletVersion servletVersion) {
        this.renderProcessor = renderProcessor;
        this.servletVersion = servletVersion;
        this.invokerSupplier = invokerSupplier;
        this.filterArrays = (filterArrays == null) ? new FilterDef[0] : filterArrays;
    }

    /** 调用目标 */
    public Future<Object> invoke(FilterChain chain) {
        Invoker invoker = this.invokerSupplier.get();
        Mapping ownerMapping = invoker.ownerMapping();
        HttpServletRequest httpRequest = invoker.getHttpRequest();
        Method targetMethod = ownerMapping.findMethod(httpRequest);

        // .异步调用
        final BasicFuture<Object> future = new BasicFuture<>();
        boolean needAsync = ownerMapping.isAsync(httpRequest);
        ServletVersion version = this.servletVersion;
        if (version.ge(ServletVersion.V3_0) && needAsync) {
            // .必须满足: Servlet3.x、环境支持异步Servlet、目标开启了Servlet3
            AsyncContext asyncContext = httpRequest.startAsync(httpRequest, invoker.getHttpResponse());
            asyncContext.start(new AsyncInvocationWorker(asyncContext, targetMethod) {
                public void doWork(Method targetMethod) throws Throwable {
                    future.completed(invoke(targetMethod, invoker));
                }

                @Override
                public void doWorkWhenError(Method targetMethod, Throwable e) {
                    future.failed(e);
                }
            });
            return future;
        }

        // .同步调用
        try {
            Object invoke = invoke(targetMethod, invoker);
            future.completed(invoke);
        } catch (Throwable e) {
            future.failed(e);
        }
        return future;
    }

    /** 执行调用 */
    private Object invoke(final Method targetMethod, final Invoker invoker) throws Throwable {
        // .初始化 Controller
        final Object targetObject = invoker.getAppContext().getInstance(invoker.ownerMapping().getTargetType());
        if (targetObject instanceof Controller controller) {
            controller.initController(invoker);
        }
        if (targetObject == null) {
            throw new NullPointerException("mappingToDefine newInstance is null.");
        }

        // .准备过滤器链
        final InvokerChain ic = i -> {
            // 设置contentType
            String contentType = i.contentType();
            if (StringUtils.isNotBlank(contentType)) {
                if (!i.getHttpResponse().isCommitted()) {
                    i.getHttpResponse().setContentType(contentType);
                }
            }

            // 执行调用
            try {
                final Object[] resolveParamsArrays = this.resolveParams(i, targetMethod);
                Object result = targetMethod.invoke(targetObject, resolveParamsArrays);
                i.put(Invoker.RETURN_DATA_KEY, result);
                return result;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };

        // .业务过滤链，末端执行 Action 和返回值渲染
        final InvokerChain last = i -> this.renderProcessor.invoke(i, ic);
        return new InvokerChainInvocation(this.filterArrays, last).doNext(invoker);
    }
}
