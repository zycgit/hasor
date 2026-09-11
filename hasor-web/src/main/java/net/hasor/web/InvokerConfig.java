/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
import java.util.Enumeration;
import net.hasor.core.AppContext;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
public interface InvokerConfig {
    /**
     * Returns a <code>String</code> containing the value of the
     * named initialization parameter, or <code>null</code> if
     * the initialization parameter does not exist.
     * @param name a <code>String</code> specifying the name of the
     * initialization parameter
     * @return a <code>String</code> containing the value of the
     * initialization parameter, or <code>null</code> if
     * the initialization parameter does not exist
     */
    String getInitParameter(String name);

    /**
     * Returns the names of the filter's initialization parameters
     * as an <code>Enumeration</code> of <code>String</code> objects,
     * or an empty <code>Enumeration</code> if the filter has
     * no initialization parameters.
     * @return an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of the filter's initialization parameters
     */
    Enumeration<String> getInitParameterNames();

    AppContext getAppContext();
}
