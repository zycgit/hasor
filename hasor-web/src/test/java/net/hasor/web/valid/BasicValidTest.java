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

public class BasicValidTest extends AbstractTest {
    @Test
    public void basic_test_1() throws Throwable {
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/valid_param.do").with(ValidRequestFieldAction.class);
        }, servlet25("/"), LoadModule.Web, LoadModule.Valid);
        //
        {
            HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/valid_param.do?byteParam=123&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
            assert ((Integer) ((Map) o).get("intParam")) == 321;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 2;
            assert !((Boolean) ((Map) o).get("doValid"));
        }
        {
            HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/valid_param.do?byteParam=2&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 2;
            assert ((Integer) ((Map) o).get("intParam")) == 321;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 1;
            assert !((Boolean) ((Map) o).get("doValid"));
        }
        {
            HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/valid_param.do?byteParam=2&intParam=5&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 2;
            assert ((Integer) ((Map) o).get("intParam")) == 5;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 0;
            assert ((Boolean) ((Map) o).get("doValid"));
        }
        //
        //
        {
            HttpServletRequest request = mockRequest("get", new URL("http://www.hasor.net/valid_param.do?byteParam=123&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
            assert ((Integer) ((Map) o).get("intParam")) == 321;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 2;
            assert !((Boolean) ((Map) o).get("doValid"));
        }
        {
            HttpServletRequest request = mockRequest("get", new URL("http://www.hasor.net/valid_param.do?byteParam=2&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 2;
            assert ((Integer) ((Map) o).get("intParam")) == 321;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 1;
            assert !((Boolean) ((Map) o).get("doValid"));
        }
        {
            HttpServletRequest request = mockRequest("get", new URL("http://www.hasor.net/valid_param.do?byteParam=2&intParam=5&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            assert (Byte) ((Map) o).get("byteParam") == (byte) 2;
            assert ((Integer) ((Map) o).get("intParam")) == 5;
            assert ((List) ((Map) o).get("validErrorsOfString")).size() == 0;
            assert ((Boolean) ((Map) o).get("doValid"));
        }
    }

    @Test
    public void basic_test_2() throws Throwable {
        AppContext appContext = buildWebAppContext("/META-INF/hasor-framework/web-hconfig.xml", apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).mappingTo("/valid_param.do").with(ValidRequestFieldAction.class);
        }, servlet25("/"), LoadModule.Web, LoadModule.Valid);
        //
        {
            HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/valid_param.do?byteParam=123&intParam=321&strParam=5678"));
            Object o = callInvoker(appContext, request);
            assert o instanceof Map;
            List validErrorsOfString = (List) ((Map) o).get("validErrorsOfString");
            List validErrorsOfMessage = (List) ((Map) o).get("validErrorsOfMessage");
            assert validErrorsOfMessage.get(0) instanceof Message;
            assert validErrorsOfMessage.get(1) instanceof Message;
            assert validErrorsOfString.get(0).toString().equals("max out of 10");
            assert validErrorsOfString.get(1).toString().equals("max out of 10");
            assert validErrorsOfMessage.get(0).toString().equals("max out of 10");
            assert validErrorsOfMessage.get(1).toString().equals("max out of 10");
            assert ((Message) validErrorsOfMessage.get(1)).getParameters().length == 1;
            assert ((Message) validErrorsOfMessage.get(1)).getParameters()[0].equals(10);
            assert ((Message) validErrorsOfMessage.get(1)).getMessageTemplate().equals("max out of %s");
        }
    }
}
