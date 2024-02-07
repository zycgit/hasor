/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.core.spi.container;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextWarp;
import net.hasor.core.Hasor;
import net.hasor.test.core.aop.ignore.types.GrandFatherBean;
import net.hasor.test.core.aop.ignore.types.JamesBean;
import net.hasor.test.core.aop.ignore.types.WilliamSonBean;
import net.hasor.test.core.basic.pojo.PojoBean;
import org.junit.Test;

import java.util.Map;

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
        assert typeMap.size() == 9;
        assert typeMap.containsKey(PojoBean.class);
        assert typeMap.containsKey(GrandFatherBean.class);
        assert typeMap.get(PojoBean.class) != null;
        assert typeMap.get(GrandFatherBean.class) != null;
        //
        Map<String, Object> beanMap = appContext.toBeanMap();
        assert beanMap.size() == 9;
        assert beanMap.containsKey("net.hasor.core.EventContext");
        assert beanMap.containsKey("net.hasor.core.Settings");
        assert beanMap.containsKey("pojo");
        assert beanMap.containsKey("net.hasor.core.spi.SpiTrigger");
        assert beanMap.containsKey("net.hasor.core.AppContext");
    }
}
