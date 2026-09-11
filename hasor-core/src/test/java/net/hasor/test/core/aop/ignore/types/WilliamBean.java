/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.aop.ignore.types;
import net.hasor.core.IgnoreProxy;

/**
 * 威廉 不接受Aop，并且影响后代
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年1月12日
 */
@IgnoreProxy()
public class WilliamBean extends GrandFatherBean {
}
