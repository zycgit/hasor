/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.function.Property;

/**
 * 提供metaData。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年7月3日
 */
public class MetaDataAdapter extends Observable {
    private final Map<String, Object> metaData = new HashMap<>();

    public void setMetaData(final String key, final Object value) {
        this.metaData.put(key, value);
    }

    public Object getMetaData(final String key) {
        return this.metaData.get(key);
    }

    public void removeMetaData(String key) {
        this.metaData.remove(key);
    }

    public String toString() {
        Map<String, Property> propertys = BeanUtils.getPropertyFunc(this.getClass());
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName()).append("{");
        for (String key : propertys.keySet()) {
            Object var = propertys.get(key).get(this);
            builder = builder.append(key).append("=").append(var).append(" ,");
        }
        builder.append("}");
        return builder.toString();
    }

    protected void notify(NotifyData notifyData) {
        setChanged();
        this.notifyObservers(notifyData);
    }
}
