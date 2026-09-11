/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.spi;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.core.AppContext;

/**
 * 在所有处理之后
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2019-09-10
 */
public interface AfterResponseListener extends java.util.EventListener {
    /** 在所有处理之后 */
    void doListener(AppContext appContext, HttpServletRequest request, HttpServletResponse response, Object invokerResult);
}
