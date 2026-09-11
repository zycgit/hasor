/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.mime;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletContext;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.core.AppContext;
import net.hasor.web.AbstractTest;
import net.hasor.web.MimeType;
import net.hasor.web.WebApiBinder;
import net.hasor.web.startup.RuntimeListener;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class MimeTest extends AbstractTest {
    public static final String NET_HASOR_WEB_MIME_MIME_TYPES_XML = "/net_hasor_web_mime/mime.types.xml";

    @Test
    public void mimeTest_1() {
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).addMimeType("afm", "abcdefg");
            apiBinder.tryCast(WebApiBinder.class).loadMimeType(NET_HASOR_WEB_MIME_MIME_TYPES_XML);
        }, servlet30("/"), LoadModule.Web);
        //
        MimeType mimeType = appContext.getInstance(MimeType.class);
        assert mimeType.getMimeType("afm").equals("abcdefg");
        assert mimeType.getMimeType("ass") == null;
        assert mimeType.getMimeType("test").equals("测试类型测试类型");
    }

    @Test
    public void mimeTest_2() throws Throwable {
        MimeTypeSupplier mimeType = null;
        //
        mimeType = new MimeTypeSupplier(PowerMockito.mock(ServletContext.class));
        mimeType.loadResource(NET_HASOR_WEB_MIME_MIME_TYPES_XML);
        assert mimeType.getMimeType("ass") == null;
        assert mimeType.getMimeType("test").equals("测试类型测试类型");
        //
        mimeType = new MimeTypeSupplier(PowerMockito.mock(ServletContext.class));
        mimeType.loadStream(ResourcesUtils.getResourceAsStream(NET_HASOR_WEB_MIME_MIME_TYPES_XML));
        assert mimeType.getMimeType("ass") == null;
        assert mimeType.getMimeType("test").equals("测试类型测试类型");
        //
        mimeType = new MimeTypeSupplier(PowerMockito.mock(ServletContext.class));
        mimeType.loadReader(new InputStreamReader(ResourcesUtils.getResourceAsStream(NET_HASOR_WEB_MIME_MIME_TYPES_XML)));
        assert mimeType.getMimeType("ass") == null;
        assert mimeType.getMimeType("test").equals("测试类型测试类型");
    }

    @Test
    public void mimeTest_3() throws IOException {
        ServletContext servletContext = servlet30("/");
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).addMimeType("7z", "7z7z7z");
        }, servletContext, LoadModule.Web);
        PowerMockito.when(servletContext.getAttribute(RuntimeListener.AppContextName)).thenReturn(appContext);
        PowerMockito.when(servletContext.getMimeType("afm")).thenReturn("form_mock");
        PowerMockito.when(servletContext.getMimeType("afmamf")).thenReturn("form_mock");
        //
        MimeType mimeType = appContext.getInstance(MimeType.class);
        assert mimeType.getMimeType("afm").equals("application/x-font-type1");
        assert mimeType.getMimeType("afmamf").equals("form_mock");
        assert mimeType.getMimeType("3dml").equals("text/vnd.in3d.3dml");
        assert mimeType.getMimeType("7z").equals("7z7z7z");
        //
        MimeTypeSupplier mimeType2 = new MimeTypeSupplier(PowerMockito.mock(ServletContext.class));
        mimeType2.loadStream(ResourcesUtils.getResourceAsStream("/META-INF/mime.types.xml"));
        //
        assert mimeType2.getMimeType("afm").equals("application/x-font-type1");
    }
}
