/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web;
/**
 * servlet版本
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-26
 */
public enum ServletVersion {
    V2_3(23),   //
    V2_4(24),   //
    V2_5(25),   //
    V3_0(30),   //
    V3_1(31),   //
    ;
    private final int version;

    ServletVersion(int version) {
        this.version = version;
    }

    /** 大于 */
    public boolean gt(ServletVersion otherVersion) {
        return this.version > otherVersion.version;
    }

    /** 大于等于 */
    public boolean ge(ServletVersion otherVersion) {
        return this.version >= otherVersion.version;
    }

    /** 等于 */
    public boolean eq(ServletVersion otherVersion) {
        return this.version == otherVersion.version;
    }

    /** 小于 */
    public boolean lt(ServletVersion otherVersion) {
        return this.version < otherVersion.version;
    }

    /** 小于等于 */
    public boolean le(ServletVersion otherVersion) {
        return this.version <= otherVersion.version;
    }
}
