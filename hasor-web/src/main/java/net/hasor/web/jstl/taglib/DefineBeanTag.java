/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.jstl.taglib;
import javax.servlet.jsp.tagext.Tag;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-24
 */
public class DefineBeanTag extends AbstractTag {
    private static final long serialVersionUID = 8066383523368039588L;

    @Override
    public int doStartTag() {
        verifyAttribute(AttributeNames.Var, AttributeNames.BeanID);
        storeToVar(getAppContext().getBindInfo(this.getBeanID()));
        return Tag.SKIP_BODY;
    }
}
