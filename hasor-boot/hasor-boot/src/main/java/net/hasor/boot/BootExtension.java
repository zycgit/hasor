/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;

/** 通过 Java SPI 加入启动链，可在应用容器创建之前准备外部环境。 */
public interface BootExtension {
    default int order() {
        return 0;
    }

    BootLauncher configure(BootConfiguration configuration, BootLauncher next);
}
