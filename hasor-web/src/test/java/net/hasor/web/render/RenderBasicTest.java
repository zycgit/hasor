/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.AppContext;
import net.hasor.web.AbstractTest;
import net.hasor.web.Invoker;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class RenderBasicTest extends AbstractTest {
    @Test
    public void renderInvoker_params() throws Throwable {
        //
        HttpServletRequest request = mockRequest("get", new URL("http://www.hasor.net/abc.do?a=1&b=2"));
        Invoker invoker = PowerMockito.mock(Invoker.class);
        PowerMockito.when(invoker.getHttpRequest()).thenReturn(request);
        AppContext appContext = PowerMockito.mock(AppContext.class);
        Settings settings = PowerMockito.mock(Settings.class);
        PowerMockito.when(invoker.getAppContext()).thenReturn(appContext);
        PowerMockito.when(appContext.getSettings()).thenReturn(settings);
        RenderInvokerSupplier supplier = new RenderInvokerSupplier(invoker);
        //
        assert supplier.get("req_a").equals("1");
        assert supplier.get("req_b").equals("2");
    }
}
