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
 * MimeType类型查询接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年2月11日
 */
public interface MimeType {
    /** 获取MimeType类型 */
    String getMimeType(String suffix);
}
