/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.jstl;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import net.hasor.core.AppContext;
import net.hasor.test.web.actions.mapping.ArgsMappingAction;
import net.hasor.web.AbstractTest;
import net.hasor.web.WebApiBinder;
import net.hasor.web.jstl.taglib.DefineBeanTag;
import net.hasor.web.jstl.taglib.DefineBindTag;
import net.hasor.web.jstl.taglib.DefineTypeTag;
import net.hasor.web.startup.RuntimeListener;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class JstlTest extends AbstractTest {
    @Test
    public void jstlTest_1() {
        ServletContext servletContext = servlet30("/");
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(ArgsMappingAction.class);
            apiBinder.bindType(String.class).idWith("abc").toInstance("abcdefg");
        }, servletContext, LoadModule.Web);
        PowerMockito.when(servletContext.getAttribute(RuntimeListener.AppContextName)).thenReturn(appContext);
        PageContext pageContext = PowerMockito.mock(PageContext.class);
        PowerMockito.when(pageContext.getServletContext()).thenReturn(appContext.getInstance(ServletContext.class));
        //
        DefineBeanTag tag = new DefineBeanTag();
        tag.setPageContext(pageContext);
        //
        try {
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param var is null.");
        }
        //
        try {
            tag.setVar("abc");
            assert tag.getVar().equals("abc");
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param beanID is null.");
        }
        //
        try {
            tag.setBeanID("abc");
            assert tag.getBeanID().equals("abc");
            assert tag.doStartTag() == Tag.SKIP_BODY;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        //
        tag.release();
        assert true;
    }

    @Test
    public void jstlTest_2() {
        ServletContext servletContext = servlet30("/");
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(ArgsMappingAction.class);
            apiBinder.bindType(String.class).idWith("my_name_is").toInstance("abcdefg");
        }, servletContext, LoadModule.Web);
        PowerMockito.when(servletContext.getAttribute(RuntimeListener.AppContextName)).thenReturn(appContext);
        PageContext pageContext = PowerMockito.mock(PageContext.class);
        PowerMockito.when(pageContext.getServletContext()).thenReturn(appContext.getInstance(ServletContext.class));
        //
        DefineBindTag tag = new DefineBindTag();
        tag.setPageContext(pageContext);
        //
        try {
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param var is null.");
        }
        //
        try {
            tag.setVar("abc");
            assert tag.getVar().equals("abc");
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param bindType is null.");
        }
        //
        try {
            tag.setName("my_name_is");
            assert tag.getName().equals("my_name_is");
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param bindType is null.");
        }
        //
        try {
            tag.setBindType("abc.abc.abc.String");
            assert tag.doStartTag() == Tag.SKIP_BODY;
            assert false;
        } catch (JspException e) {
            assert e.getCause() instanceof ClassNotFoundException;
        }
        //
        try {
            tag.setBindType("java.lang.String");
            assert tag.getBindType().equals("java.lang.String");
        } catch (Exception e) {
            assert e.getMessage().contains("tag param name is null.");
        }
        //
        try {
            tag.setName("my_name_is");
            assert tag.getName().equals("my_name_is");
            assert tag.doStartTag() == Tag.SKIP_BODY;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        //
        tag.release();
        assert true;
    }

    @Test
    public void jstlTest_3() {
        ServletContext servletContext = servlet30("/");
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(ArgsMappingAction.class);
            apiBinder.bindType(String.class).toInstance("abcdefg");
        }, servletContext, LoadModule.Web);
        PowerMockito.when(servletContext.getAttribute(RuntimeListener.AppContextName)).thenReturn(appContext);
        PageContext pageContext = PowerMockito.mock(PageContext.class);
        PowerMockito.when(pageContext.getServletContext()).thenReturn(appContext.getInstance(ServletContext.class));
        //
        DefineTypeTag tag = new DefineTypeTag();
        tag.setPageContext(pageContext);
        //
        try {
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param var is null.");
        }
        //
        try {
            tag.setVar("abc");
            assert tag.getVar().equals("abc");
            tag.doStartTag();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("tag param bindType is null.");
        }
        //
        try {
            tag.setBindType("java.lang.String");
            assert tag.getBindType().equals("java.lang.String");
            assert tag.doStartTag() == Tag.SKIP_BODY;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        //
        try {
            tag.setBindType("abc.abc.abc.String");
            assert tag.doStartTag() == Tag.SKIP_BODY;
            assert false;
        } catch (JspException e) {
            assert e.getCause() instanceof ClassNotFoundException;
        }
        //
        tag.release();
        assert true;
    }
}
