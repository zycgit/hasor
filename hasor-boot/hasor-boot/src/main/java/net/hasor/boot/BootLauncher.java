/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot;
import java.util.List;
import net.hasor.core.Module;

/** 启动链的下一阶段；环境对象由扩展提供，普通应用传 null。 */
@FunctionalInterface
public interface BootLauncher {
    BootApplication start(Object environment, List<Module> modules) throws Exception;
}
