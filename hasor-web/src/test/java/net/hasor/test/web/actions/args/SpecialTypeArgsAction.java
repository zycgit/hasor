/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.args;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.web.Invoker;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.QueryParameter;
import net.hasor.web.render.RenderInvoker;

public class SpecialTypeArgsAction {
    @Any
    public Map<String, Object> execute(//
            Invoker invoker, RenderInvoker renderInvoker,    //
            ServletRequest servletRequest, HttpServletRequest httpServletRequest,  //
            ServletResponse servletResponse, HttpServletResponse httpServletResponse, //
            HttpSession httpSession, ServletContext servletContext,//
            AppContext appContext, Settings settings,//
            boolean bool, @QueryParameter("string") String string) {
        return new HashMap<String, Object>() {{
            put("invoker", invoker);
            put("renderInvoker", renderInvoker);
            put("servletRequest", servletRequest);
            put("httpServletRequest", httpServletRequest);
            put("servletResponse", servletResponse);
            put("httpServletResponse", httpServletResponse);
            put("httpSession", httpSession);
            put("servletContext", servletContext);
            put("appContext", appContext);
            put("settings", settings);
            //
            put("bool", bool);
            put("string", string);
        }};
    }
}
