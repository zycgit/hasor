/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.valid;
import net.hasor.web.annotation.RequestParameter;

public class ValidRequestFieldBean {
    @RequestParameter("byteParam")
    private byte   byteParam;
    @RequestParameter("intParam")
    private int    intParam;
    @RequestParameter("strParam")
    private String strParam;
    @RequestParameter("")
    private String eptParam;

    public byte getByteParam() {
        return byteParam;
    }

    public int getIntParam() {
        return intParam;
    }

    public String getStrParam() {
        return strParam;
    }

    public String getEptParam() {
        return eptParam;
    }
}
