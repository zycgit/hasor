/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web.health;

/** 注册到 Hasor 容器的健康检查项；实现应线程安全并为外部调用设置超时。 */
@FunctionalInterface
public interface HealthCheck {
    /** 返回 false 或抛出异常均表示不健康。 */
    boolean check() throws Exception;
}
