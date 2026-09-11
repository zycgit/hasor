/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.valid;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.hasor.core.AppContext;
import net.hasor.test.web.actions.valid.ValidRequestFieldAction;
import net.hasor.web.AbstractTest;
import net.hasor.web.WebApiBinder;
import org.junit.Test;

public class UnexpectedValidTest extends AbstractTest {
    @Test
    public void unexpected_1() throws Throwable {
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/valid_param.do").with(ValidRequestFieldAction.class);
        }, servlet25("/"), LoadModule.Web, LoadModule.Valid);
        //
        HttpServletRequest request = mockRequest("put", new URL("http://www.hasor.net/valid_param.do?byteParam=123&intParam=321&strParam=5678"));
        Object o = callInvoker(appContext, request);
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Integer) ((Map) o).get("intParam")) == 321;
        assert ((List) ((Map) o).get("validErrorsOfString")).size() == 4;
        assert !((Boolean) ((Map) o).get("doValid"));
        //
        List validErrorsOfString = (List) ((Map) o).get("validErrorsOfString");
        assert validErrorsOfString.get(0).equals("max out of %s");
        assert validErrorsOfString.get(1).equals("max out of %d");
        //
        assert ((Map) o).get("err3_1") != null;
        assert ((Map) o).get("err3_1").equals("message 1");
        //
        assert ((List) ((Map) o).get("err3_2")).size() == 2;
        assert ((List) ((Map) o).get("err3_2")).get(0).equals("message 1");
        assert ((List) ((Map) o).get("err3_2")).get(1).equals("message 2");
    }

    @Test
    public void unexpected_2() throws Throwable {
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/valid_param.do").with(ValidRequestFieldAction.class);
        }, servlet25("/"), LoadModule.Web, LoadModule.Valid);
        //
        {
            HttpServletRequest request = mockRequest("head", new URL("http://www.hasor.net/valid_param.do?byteParam=123&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert !((Boolean) ((Map) o).get("err3_res_before"));
            assert (Boolean) ((Map) o).get("err3_res_after");
        }
    }
}
