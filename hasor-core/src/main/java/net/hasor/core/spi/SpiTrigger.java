/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package net.hasor.core.spi;
import java.util.EventListener;
import net.hasor.core.spi.SpiCaller.SpiCallerWithoutResult;

/**
 * SPI 触发器：SPI 的本真意图是在应用流程执行的过程中，安插一些扩展点。
 * - 让应用有机会在正常的流程中对外提供接口扩展能力。
 * - 通过 SPI 接口达到获得流程内部状态 或 干预程序流程中变量值的目的。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-11-8
 */
public interface SpiTrigger {
    /** 判断某类 SPI 是否有注册 */
    boolean hasSpi(Class<? extends EventListener> spiType);

    /** 判断某类 SPI 是否有注册了仲裁 */
    boolean hasJudge(Class<? extends EventListener> spiJudge);

    /**
     * 通知型 SPI：保证所有 SPI 都会被触发，每一个SPI监听器 都可以拿到最初的值。
     * - 是异步工作思想
     * @param spiType SPI 接口类型
     * @param spiCaller spiCaller
     */
    default <T extends EventListener> void notifySpiWithoutResult(Class<T> spiType, SpiCallerWithoutResult<T> spiCaller) {
        notifyWithoutJudge(spiType, spiCaller, null);
    }

    /**
     * 通知型 SPI：保证所有 SPI 都会被触发，每一个SPI监听器 都可以拿到最初的值。
     * - 异步工作思想
     * @param spiType SPI 接口类型
     * @param spiCaller spiCaller
     */
    <R, T extends EventListener> R notifyWithoutJudge(Class<T> spiType, SpiCaller<T, R> spiCaller, R defaultResult);

    /**
     * 通知型 SPI：保证所有 SPI 都会被触发，每一个SPI监听器 都可以拿到最初的值。
     * - 异步工作思想
     * @param spiType SPI 接口类型
     * @param spiCaller spiCaller
     */
    <R, T extends EventListener> R notifySpi(Class<T> spiType, SpiCaller<T, R> spiCaller, R defaultResult);

    /**
     * 链型 SPI： 监听器工作模式类似Aop拦截器，下一个 SPI监听器 可以获取上一个 SPI监听器的值。
     * - 同步工作思想
     * @param spiType SPI 接口类型
     * @param spiCaller spiCaller
     */
    default <R, T extends EventListener> R chainSpi(Class<T> spiType, SpiCaller<T, R> spiCaller) {
        return chainSpi(spiType, spiCaller, null);
    }

    /**
     * 链型 SPI： 下一个 SPI监听器 可以获取上一个 SPI监听器的值。
     * - 同步工作思想
     * @param spiType SPI 接口类型
     * @param spiCaller spiCaller
     * @param defaultResult 默认值
     */
    <R, T extends EventListener> R chainSpi(Class<T> spiType, SpiCaller<T, R> spiCaller, R defaultResult);
}
