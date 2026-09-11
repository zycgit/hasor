/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.aop;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.hasor.cobble.dynamic.Aop;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.core.ApiBinder;
import net.hasor.core.HasorUtils;
import net.hasor.core.Module;

/**
 * 提供 <code>@Aop</code>注解 功能支持。
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2013-9-13
 */
public class AopModule implements Module {
    private static final Logger logger = LoggerFactory.getLogger(AopModule.class);

    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //Aop拦截器
        Predicate<Class<?>> matcherClass = Matchers.annotatedWithClass(Aop.class);//
        Predicate<Method> matcherMethod = Matchers.annotatedWithMethod(Aop.class);//
        //
        logger.debug(String.format("aop -> matcherClass = %s, matcherMethod =%s.", matcherClass, matcherMethod));
        AopInterceptor aopInterceptor = HasorUtils.autoAware(apiBinder.getEventContext(), new AopInterceptor());
        apiBinder.bindInterceptor(matcherClass, matcherMethod, aopInterceptor);
    }
}
