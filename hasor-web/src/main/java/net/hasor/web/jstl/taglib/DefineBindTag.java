/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.jstl.taglib;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import net.hasor.cobble.ClassUtils;
import net.hasor.core.AppContext;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-24
 */
public class DefineBindTag extends AbstractTag {
    private static final long serialVersionUID = -7899624524135156746L;

    @Override
    public int doStartTag() throws JspException {
        verifyAttribute(AttributeNames.Var, AttributeNames.BindType, AttributeNames.Name);
        //
        try {
            AppContext appContext = getAppContext();
            ClassLoader classLoader = ClassUtils.getClassLoader(appContext.getClassLoader());
            Class<?> defineType = Class.forName(this.getBindType(), false, classLoader);
            storeToVar(appContext.findBindingBean(this.getName(), defineType));
            return Tag.SKIP_BODY;
        } catch (ClassNotFoundException e) {
            throw new JspException(e);
        }
    }
}
