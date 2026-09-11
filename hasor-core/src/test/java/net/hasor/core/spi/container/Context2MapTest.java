/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi.container;

import java.util.Map;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextWarp;
import net.hasor.core.Hasor;
import net.hasor.test.core.aop.ignore.types.GrandFatherBean;
import net.hasor.test.core.aop.ignore.types.JamesBean;
import net.hasor.test.core.aop.ignore.types.WilliamSonBean;
import net.hasor.test.core.basic.pojo.PojoBean;
import org.junit.Test;

public class Context2MapTest {
    @Test
    public void contextTest1() {
        AppContext appContext = new AppContextWarp(Hasor.create().build(apiBinder -> {
            apiBinder.bindType(PojoBean.class).bothWith("pojo");
            apiBinder.bindType(GrandFatherBean.class).nameWith("james").to(JamesBean.class);
            apiBinder.bindType(GrandFatherBean.class).nameWith("william").to(WilliamSonBean.class);
        }));
        //
        Map<String, Object> objectMap = appContext.toNameMap(GrandFatherBean.class);
        assert objectMap.size() == 2;
        assert objectMap.containsKey("james");
        assert objectMap.containsKey("william");
        //
        Map<Class<?>, Object> typeMap = appContext.toTypeMap();
        assert typeMap.containsKey(PojoBean.class);
        assert typeMap.containsKey(GrandFatherBean.class);
        assert typeMap.get(PojoBean.class) != null;
        assert typeMap.get(GrandFatherBean.class) != null;
        //
        Map<String, Object> beanMap = appContext.toBeanMap();
        assert beanMap.containsKey("net.hasor.core.EventContext");
        assert beanMap.containsKey("net.hasor.cobble.setting.Settings");
        assert beanMap.containsKey("pojo");
        assert beanMap.containsKey("net.hasor.core.spi.SpiTrigger");
        assert beanMap.containsKey("net.hasor.core.AppContext");
    }
}
