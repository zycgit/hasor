/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.After;
import org.junit.Before;

/** 为三个容器提供一致的测试应用类加载环境。 */
public abstract class ContainerIntegrationTest {
    private ClassLoader    originalClassLoader;
    private URLClassLoader applicationClassLoader;

    @Before
    public void prepareApplicationClassLoader() {
        this.originalClassLoader = Thread.currentThread().getContextClassLoader();
        // Java 17 AppClassLoader 不暴露 JAR 列表；显式提供测试应用位置供包扫描使用。
        URL applicationLocation = ContainerIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation();
        this.applicationClassLoader = new URLClassLoader(new URL[] { applicationLocation }, this.originalClassLoader);
        Thread.currentThread().setContextClassLoader(this.applicationClassLoader);
    }

    @After
    public void releaseApplicationClassLoader() throws Exception {
        // JUnit 在子类停止容器之后执行父类清理。
        if (this.applicationClassLoader != null) {
            Thread.currentThread().setContextClassLoader(this.originalClassLoader);
            this.applicationClassLoader.close();
        }
    }
}
