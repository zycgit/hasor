/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.basic.inject.constructor;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import net.hasor.core.InjectSettings;
import net.hasor.test.core.enums.SelectEnum;

// 8 种基本类型和其包装类型，以及4种时间类型和常用的枚举、字符串
public class ConstructorBeanByInjectSettingEnvValue {
    private final byte byteValue;
    private final Byte byteValue2;
    //
    private final short shortValue;
    private final Short shortValue2;
    //
    private final int   intValue;
    private final Integer intValue2;
    //
    private final long    longValue;
    private final Long    longValue2;
    //
    private final float floatValue;
    private final Float floatValue2;
    //
    private final double doubleValue;
    private final Double doubleValue2;
    //
    private final boolean booleanValue;
    private final Boolean booleanValue2;
    //
    private final char    charValue;
    private final Character charValue2;
    //
    private final Date      dateValue1;
    private final java.sql.Date dateValue2;
    private final Time          dateValue3;
    private final Timestamp     dateValue4;
    //
    private final String    stringValue;
    private final SelectEnum enumValue;

    //
    public ConstructorBeanByInjectSettingEnvValue(//
            @InjectSettings("${byteValue}") byte byteValue             //
            , @InjectSettings("${byteValue}") Byte byteValue2          //
            , @InjectSettings("${shortValue}") short shortValue        //
            , @InjectSettings("${shortValue}") Short shortValue2       //
            , @InjectSettings("${intValue}") int intValue              //
            , @InjectSettings("${intValue}") Integer intValue2         //
            , @InjectSettings("${longValue}") long longValue           //
            , @InjectSettings("${longValue}") Long longValue2          //
            , @InjectSettings("${floatValue}") float floatValue        //
            , @InjectSettings("${floatValue}") Float floatValue2       //
            , @InjectSettings("${doubleValue}") double doubleValue     //
            , @InjectSettings("${doubleValue}") Double doubleValue2    //
            , @InjectSettings("${booleanValue}") boolean booleanValue  //
            , @InjectSettings("${booleanValue}") Boolean booleanValue2 //
            , @InjectSettings("${charValue}") char charValue           //
            , @InjectSettings("${charValue}") Character charValue2     //
            , @InjectSettings("${dateValue}") Date dateValue1          //
            , @InjectSettings("${dateValue}") java.sql.Date dateValue2 //
            , @InjectSettings("${dateValue}") Time dateValue3          //
            , @InjectSettings("${dateValue}") Timestamp dateValue4     //
            , @InjectSettings("${stringValue}") String stringValue     //
            , @InjectSettings("${enumValue}") SelectEnum enumValue) {
        //
        this.byteValue = byteValue;
        this.byteValue2 = byteValue2;
        this.shortValue = shortValue;
        this.shortValue2 = shortValue2;
        this.intValue = intValue;
        this.intValue2 = intValue2;
        this.longValue = longValue;
        this.longValue2 = longValue2;
        this.floatValue = floatValue;
        this.floatValue2 = floatValue2;
        this.doubleValue = doubleValue;
        this.doubleValue2 = doubleValue2;
        this.booleanValue = booleanValue;
        this.booleanValue2 = booleanValue2;
        this.charValue = charValue;
        this.charValue2 = charValue2;
        this.dateValue1 = dateValue1;
        this.dateValue2 = dateValue2;
        this.dateValue3 = dateValue3;
        this.dateValue4 = dateValue4;
        this.stringValue = stringValue;
        this.enumValue = enumValue;
    }

    //
    //
    //
    public byte getByteValue() {
        return byteValue;
    }

    public Byte getByteValue2() {
        return byteValue2;
    }

    public short getShortValue() {
        return shortValue;
    }

    public Short getShortValue2() {
        return shortValue2;
    }

    public int getIntValue() {
        return intValue;
    }

    public Integer getIntValue2() {
        return intValue2;
    }

    public long getLongValue() {
        return longValue;
    }

    public Long getLongValue2() {
        return longValue2;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public Float getFloatValue2() {
        return floatValue2;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public Double getDoubleValue2() {
        return doubleValue2;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public Boolean getBooleanValue2() {
        return booleanValue2;
    }

    public char getCharValue() {
        return charValue;
    }

    public Character getCharValue2() {
        return charValue2;
    }

    public Date getDateValue1() {
        return dateValue1;
    }

    public java.sql.Date getDateValue2() {
        return dateValue2;
    }

    public Time getDateValue3() {
        return dateValue3;
    }

    public Timestamp getDateValue4() {
        return dateValue4;
    }

    public String getStringValue() {
        return stringValue;
    }

    public SelectEnum getEnumValue() {
        return enumValue;
    }
}
