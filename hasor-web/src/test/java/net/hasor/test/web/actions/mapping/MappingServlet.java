/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.mapping;
import net.hasor.core.Singleton;
import net.hasor.test.web.actions.servlet.SimpleServlet;
import net.hasor.web.annotation.MappingTo;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-08
 */
@Singleton
@MappingTo("/mappingto_a.do")
public class MappingServlet extends SimpleServlet {
}
