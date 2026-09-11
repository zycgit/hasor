/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * <p>这个包提供了Hasor事件服务支持。</p>
 * Event 提供了一个简单的事件管理器。开发者可以通过 EventListener 接口编写事件处理程序。
 * 开发人员可以事件注册、引发事件。Hasor 的事件机制支持 Sync、Async 两种触发机制，它们的区别在于引发事件之后事件的处理方式上不同。
 * 对于异步事件可以通过 EventCallBackHook 接口收到事件执行过程中成功还是失败的信息。
 */
package net.hasor.core.event;
