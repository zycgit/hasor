/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.spi;
import net.hasor.web.Mapping;

/**
 * 控制器发现，每当发现一个控制器时都会调用这个接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
@FunctionalInterface
public interface MappingDiscoverer extends java.util.EventListener {
    /** 发现控制器 */
    void discover(Mapping mappingData);
}
