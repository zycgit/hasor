/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;

/**
 * 处理结果，将其 toString 并执行 Forward 操作。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-04
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@RenderType(engineType = ForwardTo.RedirectRenderEngine.class)
public @interface ForwardTo {
    class RedirectRenderEngine implements RenderEngine {
        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            Object o = invoker.get(Invoker.RETURN_DATA_KEY);
            String redirectTo = null;
            if (o != null) {
                redirectTo = o.toString();
            }
            if (StringUtils.isBlank(redirectTo)) {
                throw new NullPointerException("redirect to empty.");
            }
            HttpServletResponse httpResponse = invoker.getHttpResponse();
            if (!httpResponse.isCommitted()) {
                HttpServletRequest httpRequest = invoker.getHttpRequest();
                httpRequest.getRequestDispatcher(redirectTo).forward(invoker.getHttpRequest(), invoker.getHttpResponse());
            }
        }
    }
}
