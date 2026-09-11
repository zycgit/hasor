/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.spi;
import net.hasor.core.AppContext;
import net.hasor.core.spi.ContextInitializeListener;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年9月7日
 */
public class JdkSpiImpl implements ContextInitializeListener {
    private static boolean init = false;

    public static boolean isInit() {
        return init;
    }

    public static void resetInit() {
        init = false;
    }

    @Override
    public void doInitializeCompleted(AppContext templateAppContext) {
        init = true;
    }
}
