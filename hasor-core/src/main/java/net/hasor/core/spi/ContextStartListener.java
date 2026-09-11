/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi;
import net.hasor.core.AppContext;

/**
 * 用于容器启动事件接收
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2014-5-10
 */
public interface ContextStartListener extends java.util.EventListener {
    /** 开始进入容器启动过程。 */
    void doStart(AppContext appContext);

    /** 容器启动完成，交由一个异步线程来执行 */
    void doStartCompleted(AppContext appContext);
}
