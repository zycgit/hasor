/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi.container;
import java.util.function.Supplier;
import javax.inject.Singleton;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.provider.Scope;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.Hasor;
import net.hasor.core.spi.CollectScopeChainSpi;
import net.hasor.test.core.basic.pojo.PojoBean;
import org.junit.Test;

public class DefaultSingletonTest {
    @Test
    public void builderTest1() {
        AppContext appContext = Hasor.create().build(apiBinder -> {
            // 为每一个 apiBinder 声明的对象都设置单例
            apiBinder.bindSpiListener(CollectScopeChainSpi.class, new CollectScopeChainSpi() {
                public Supplier<Scope>[] collectScope(BindInfo<?> bindInfo, AppContext appContext, Supplier<Scope>[] suppliers) {
                    return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
                }

                public Supplier<Scope>[] collectScope(Class<?> targetType, AppContext appContext, Supplier<Scope>[] suppliers) {
                    return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
                }
            });
        });
        //
        PojoBean pojoBean1 = appContext.getInstance(PojoBean.class);
        PojoBean pojoBean2 = appContext.getInstance(PojoBean.class);
        assert pojoBean1 == pojoBean2;
        //
        appContext = Hasor.create().build();
        pojoBean1 = appContext.getInstance(PojoBean.class);
        pojoBean2 = appContext.getInstance(PojoBean.class);
        assert pojoBean1 != pojoBean2;
    }
}

class MyCollectScopeListener implements CollectScopeChainSpi {
    public Supplier<Scope>[] collectScope(BindInfo<?> bindInfo, AppContext appContext, Supplier<Scope>[] suppliers) {
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }

    public Supplier<Scope>[] collectScope(Class<?> targetType, AppContext appContext, Supplier<Scope>[] suppliers) {
        return ArrayUtils.add(suppliers, appContext.findScope(Singleton.class));
    }
}
