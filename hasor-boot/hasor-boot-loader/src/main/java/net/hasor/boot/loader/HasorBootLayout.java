/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
