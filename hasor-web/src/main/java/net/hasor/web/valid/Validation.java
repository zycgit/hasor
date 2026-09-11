/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.valid;
/**
 * 对象验证，如果验证失败返回验证消息。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public interface Validation<T> {
    /**
     * 验证逻辑
     * @param scene 场景名
     * @param dataForm 等待验证的数据。
     * @param errors 验证结果。
     */
    void doValidation(String scene, T dataForm, ValidInvoker errors);
}
