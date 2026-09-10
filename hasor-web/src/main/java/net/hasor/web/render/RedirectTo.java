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
package net.hasor.web.render;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;

/**
 * 处理结果，将其 toString 并执行 Redirect 操作。
 * @version : 2020-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@RenderType(engineType = RedirectTo.RedirectRenderEngine.class)
public @interface RedirectTo {
    /** Redirect status: 301, 302 (default), 303, 307 or 308. */
    int value() default HttpServletResponse.SC_FOUND;

    class RedirectRenderEngine implements RenderEngine {
        @Override
        public void process(RenderInvoker invoker, Writer writer) throws Throwable {
            int status = HttpServletResponse.SC_FOUND;
            if (invoker.ownerMapping() != null) {
                java.lang.reflect.Method method = invoker.ownerMapping().findMethod(invoker.getHttpRequest());
                RedirectTo annotation = method.getAnnotation(RedirectTo.class);
                if (annotation == null) {
                    annotation = method.getDeclaringClass().getAnnotation(RedirectTo.class);
                }
                if (annotation != null) {
                    status = annotation.value();
                }
            }

            if (status != 301 && status != 302 && status != 303 && status != 307 && status != 308) {
                throw new IllegalArgumentException("Unsupported redirect status: " + status);
            }

            Object o = invoker.get(Invoker.RETURN_DATA_KEY);
            String redirectTo = null;
            if (o != null) {
                redirectTo = o.toString();
            }
            if (StringUtils.isBlank(redirectTo)) {
                throw new NullPointerException("redirect to empty.");
            }
            if (redirectTo.indexOf('\r') >= 0 || redirectTo.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("Redirect location must not contain CR or LF");
            }

            HttpServletResponse httpResponse = invoker.getHttpResponse();
            if (!httpResponse.isCommitted()) {
                if (status == HttpServletResponse.SC_FOUND) {
                    httpResponse.sendRedirect(redirectTo);
                } else {
                    httpResponse.resetBuffer();
                    httpResponse.setStatus(status);
                    httpResponse.setHeader("Location", httpResponse.encodeRedirectURL(redirectTo));
                    httpResponse.flushBuffer();
                }
            }
        }
    }
}