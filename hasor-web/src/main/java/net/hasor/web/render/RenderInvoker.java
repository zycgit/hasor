/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import net.hasor.web.Invoker;

/**
 * 渲染插件 Api
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public interface RenderInvoker extends Invoker {
    /** 要渲染的资源。 */
    String renderTo();

    /** 指定要渲染的资源，并指定渲染器。 */
    void renderTo(String viewName);

    /** 指定要渲染的资源，并指定渲染器。 */
    void renderTo(String renderType, String viewName);

    /** 当前使用的渲染器。 */
    String renderType();

    /** 指定渲染器。 */
    void renderType(String renderType);

    /** 是否启用布局功能。 */
    boolean layout();

    /** 启用布局功能。 */
    void layoutEnable();

    /** 禁用布局功能。 */
    void layoutDisable();
}
