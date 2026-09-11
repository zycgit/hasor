/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.implby.middlesingleton;
import net.hasor.core.ImplBy;
import net.hasor.core.Singleton;

/**
 * Singleton 注解的声明无效，原因是已经指定了具体实现类型为：MsImplSampleBean
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-1-3
 */
@Singleton
@ImplBy(MsImplSampleBean.class)
public interface MsSampleBeanFace extends MsSampleBeanFaceByFace {
    String getName();

    int getAge();

    String getAddress();
}
