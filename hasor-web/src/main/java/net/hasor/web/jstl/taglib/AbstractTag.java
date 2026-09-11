/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.jstl.taglib;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import net.hasor.cobble.StringUtils;
import net.hasor.core.AppContext;
import net.hasor.web.startup.RuntimeListener;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-23
 */
public abstract class AbstractTag extends TagSupport {
    private static final long   serialVersionUID = 954597728447849929L;
    private              String var              = null;
    private              String beanID           = null;
    private              String name             = null;
    private              String bindType         = null;

    public String getVar() {
        return this.var;
    }

    public void setVar(final String var) {
        this.var = var;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getBindType() {
        return this.bindType;
    }

    public void setBindType(final String bindType) {
        this.bindType = bindType;
    }

    public String getBeanID() {
        return beanID;
    }

    public void setBeanID(String beanID) {
        this.beanID = beanID;
    }

    public void release() {
        this.var = null;
        this.name = null;
        this.bindType = null;
    }

    protected void verifyAttribute(AttributeNames... attrArrays) {
        for (AttributeNames attr : attrArrays) {
            if (AttributeNames.Var == attr && StringUtils.isBlank(this.var)) {
                throw new NullPointerException("tag param var is null.");
            }
            if (AttributeNames.BeanID == attr && StringUtils.isBlank(this.beanID)) {
                throw new NullPointerException("tag param beanID is null.");
            }
            if (AttributeNames.Name == attr && StringUtils.isBlank(this.name)) {
                throw new NullPointerException("tag param name is null.");
            }
            if (AttributeNames.BindType == attr && StringUtils.isBlank(this.bindType)) {
                throw new NullPointerException("tag param bindType is null.");
            }
        }
    }

    protected AppContext getAppContext() {
        ServletContext sc = this.pageContext.getServletContext();
        AppContext appContext = RuntimeListener.getAppContext(sc);
        if (appContext != null) {
            return appContext;
        }
        throw new NullPointerException("AppContext is undefined.");
    }

    protected void storeToVar(Object targetBean) {
        this.pageContext.setAttribute(this.getVar(), targetBean);
    }

    @Override
    public abstract int doStartTag() throws JspException;
}
