/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.util.Map;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import net.hasor.web.AbstractTest;
import net.hasor.web.InvokerConfig;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class ConfigTest extends AbstractTest {
    @Test
    public void configTest_1() {
        OneConfig oneConfig = new OneConfig();
        oneConfig.put("abc", "abc");
        //
        assert new OneConfig((FilterConfig) oneConfig, null).get("abc").equals("abc");
        assert new OneConfig((ServletConfig) oneConfig, null).get("abc").equals("abc");
        assert new OneConfig("", (InvokerConfig) oneConfig, null).get("abc").equals("abc");
        assert new OneConfig("", (Map) oneConfig, null).get("abc").equals("abc");
    }
}
