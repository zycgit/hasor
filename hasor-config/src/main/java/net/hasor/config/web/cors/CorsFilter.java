/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.web.cors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.cobble.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerChain;
import net.hasor.web.InvokerFilter;
import net.hasor.web.Mapping;
import net.hasor.web.annotation.HttpMethod;

/**
 * 启用跨域
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-20
 */
public class CorsFilter implements InvokerFilter {
    @Override
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        String httpMethod = invoker.getHttpRequest().getMethod();
        Mapping mapping = invoker.ownerMapping();
        if (mapping != null && mapping.findMethod(HttpMethod.OPTIONS) != null) {
            return chain.doNext(invoker);
        }
        //
        HttpServletRequest httpRequest = invoker.getHttpRequest();
        HttpServletResponse httpResponse = invoker.getHttpResponse();
        //
        String originString = httpRequest.getHeader("Origin");
        if (StringUtils.isNotBlank(originString)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", originString);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        }
        httpResponse.addHeader("Access-Control-Allow-Methods", "*");
        httpResponse.addHeader("Access-Control-Allow-Headers", "content-type");
        httpResponse.addHeader("Access-Control-Max-Age", "3600");
        //
        if (HttpMethod.OPTIONS.equalsIgnoreCase(httpMethod)) {
            return null;
        } else {
            return chain.doNext(invoker);
        }
    }
}
