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
 * 辅助{@link Inject @Inject}注解用来标识value，表示的是 ByID，还是ByName。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年7月28日
 */
public enum Type {
    /** AppContext.getInstance(bindID)方式 */
    ByID,
    /** （默认）AppContext.findBindingBean(withName, bindType)方式 */
    ByName
}
