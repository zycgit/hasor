/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.aop.fixture;
import java.util.*;
import net.hasor.cobble.dynamic.Aop;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.IgnoreProxy;

@Aop(ClassAnnoInterceptor.class)
@IgnoreProxy(ignore = false)
public class AopBean {
    public void doInit(List<String> event) {
        event.add("DO");
    }

    @Aop(MethodAnnoInterceptor.class)
    public List<?> checkBaseType(//
            boolean booleanValue, byte byteValue, short shortValue, //
            int intValue, long longValue, float floatValue, double doubleValue, char charValue) {
        return Arrays.asList(booleanValue, byteValue, shortValue, intValue, longValue, floatValue, doubleValue, charValue);
    }

    @Aop(MethodAnnoInterceptor.class)
    public boolean aBooleanValue(boolean aBooleanValue) {
        return aBooleanValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public byte aByteValue(byte aByteValue) {
        return aByteValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public short aShort(short aShortValue) {
        return aShortValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public int aIntValue(int aIntValue) {
        return aIntValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public long aLongValue(long aLongValue) {
        return aLongValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public float aFloatValue(float aFloatValue) {
        return aFloatValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public double aDoubleValue(double aDoubleValue) {
        return aDoubleValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public char aCharValue(char aCharValue) {
        return aCharValue;
    }

    @Aop(MethodAnnoInterceptor.class)
    public <T extends List<V>, V extends Settings> Map<String, Object> signatureMethod(T param1, V param2) {
        Map<String, Object> result = new HashMap<>();
        result.put("obj1", param1);
        result.put("obj2", param2);
        return result;
    }

    //
    @Aop(MethodAnnoInterceptor.class)
    public Map<String, Object> signatureMethod(List<? super Date> param1, List<? extends Date> param2) {
        Map<String, Object> result = new HashMap<>();
        result.put("obj1", param1);
        result.put("obj2", param2);
        return result;
    }
}
