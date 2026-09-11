/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.args;
import net.hasor.web.annotation.QueryParameter;

public class QueryFieldBean {
    @QueryParameter("byteParam")
    private byte   byteParam;
    @QueryParameter("intParam")
    private int    intParam;
    @QueryParameter("strParam")
    private String strParam;
    @QueryParameter("")
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
