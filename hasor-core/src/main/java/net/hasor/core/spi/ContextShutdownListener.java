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
 * 用于容器销毁事件接收
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2014-5-10
 */
public interface ContextShutdownListener extends java.util.EventListener {
    /** 开始进入容器销毁过程。 */
    void doShutdown(AppContext appContext);

    /** 容器销毁完成。 */
    void doShutdownCompleted(AppContext appContext);
}
