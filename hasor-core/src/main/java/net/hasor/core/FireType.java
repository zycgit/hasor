/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core;
/**
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2017年3月16日
 */
public enum FireType {
    /** 当遇到异常时，中断调用其它事件监听器。 */
    Interrupt,
    /** 当遇到异常时，继续调用其它事件监听器。 */
    Continue
}
