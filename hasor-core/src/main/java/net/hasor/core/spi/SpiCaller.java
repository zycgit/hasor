/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.spi;
/**
 * 执行 Spi 调用，扩展 SpiResultCaller 接口。并提供了可以无返回值的形式。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2019年06月20日
 */
public interface SpiCaller<T, R> {
    R doResultSpi(T listener, R lastResult) throws Throwable;

    interface SpiCallerWithoutResult<T> extends SpiCaller<T, Object> {
        default Object doResultSpi(T listener, Object lastResult) throws Throwable {
            this.doSpi(listener);
            return null;
        }

        void doSpi(T listener) throws Throwable;
    }
}
