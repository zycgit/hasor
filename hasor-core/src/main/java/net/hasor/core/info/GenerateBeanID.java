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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021年07月04日
 */
public class GenerateBeanID {
    private final Map<String, AtomicInteger> integerMap = new HashMap<>();

    public String generateBeanID(Class<?> bindingType) {
        String typeName = bindingType.getName();
        if (!integerMap.containsKey(typeName)) {
            integerMap.computeIfAbsent(typeName, s -> new AtomicInteger());
        } else {
            AtomicInteger integer = integerMap.get(typeName);
            typeName = typeName + "#" + integer.incrementAndGet();
        }
        return typeName;
    }
}
