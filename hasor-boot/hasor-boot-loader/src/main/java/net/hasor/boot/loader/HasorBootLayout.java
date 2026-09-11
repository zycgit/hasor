/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader;
/**
 * Hasor Boot executable archive layout constants.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
public final class HasorBootLayout {
    public static final String APP_CLASSES        = "APP-INF/classes/";
    public static final String APP_LIB            = "APP-INF/lib/";
    public static final String APP_CONFIG         = "APP-INF/hasor/";
    public static final String MANIFEST_MAIN      = "Main-Class";
    public static final String MANIFEST_START     = "Hasor-Main-Class";
    public static final String MANIFEST_CLASSPATH = "Hasor-Class-Path";

    private HasorBootLayout() {
    }
}
