/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.valid;
import java.util.ArrayList;

/**
 * 一个item下的验证信息
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年8月27日
 */
class ValidItem extends ArrayList<Message> {
    private final String key;

    public ValidItem(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public boolean isValid() {
        return this.isEmpty();
    }

    public Message firstError() {
        return (this.isEmpty()) ? null : get(0);
    }
}
