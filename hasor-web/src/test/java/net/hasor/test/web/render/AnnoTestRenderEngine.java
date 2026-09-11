/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.render;
import java.util.List;
import net.hasor.web.render.Render;

@Render("jspx")
public class AnnoTestRenderEngine extends TestRenderEngine {
    public AnnoTestRenderEngine(List<String> templateSet) {
        super(templateSet);
    }
}
