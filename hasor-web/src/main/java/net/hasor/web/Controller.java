/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
/**
 * Controller 的初始化调用接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public interface Controller {
    /** 在处理新请求之前，初始化这个控制器。 */
    void initController(Invoker invoker) throws Throwable;
}
