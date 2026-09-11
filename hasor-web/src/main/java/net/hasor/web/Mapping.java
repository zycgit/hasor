/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import net.hasor.core.BindInfo;

/**
 * 控制器映射信息
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
public interface Mapping {
    /**
     * 获取目标类型
     */
    BindInfo<?> getTargetType();

    /** 获取映射的地址 */
    String getMappingTo();

    /** 获取映射的地址的正则表达式形式 */
    String getMappingToMatches();

    /**
     * 首先测试路径是否匹配，然后判断Restful实例是否支持这个 请求方法。
     * @return 返回测试结果。
     */
    boolean matchingMapping(HttpServletRequest request);

    /** 获取方法 */
    String[] getHttpMethodSet();

    /**
     * 获取调用目标的方法
     */
    default Method findMethod(HttpServletRequest request) {
        return findMethod(request.getMethod().trim().toUpperCase());
    }

    Method findMethod(String requestMethod);

    String getSpecialContentType(String requestMethod);

    boolean isAsync(HttpServletRequest request);
}
