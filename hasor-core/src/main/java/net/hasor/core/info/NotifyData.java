/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016年10月21日
 */
public class NotifyData {
    private String key      = null;
    private Object oldValue = null;
    private Object newValue = null;

    public NotifyData(String key, Object oldValue, Object newValue) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getKey() {
        return key;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
