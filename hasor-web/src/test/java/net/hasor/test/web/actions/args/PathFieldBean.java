/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.args;
import net.hasor.web.annotation.PathParameter;

public class PathFieldBean {
    @PathParameter("byteParam")
    private byte   byteParam;
    @PathParameter("intParam")
    private int    intParam;
    @PathParameter("strParam")
    private String strParam;
    @PathParameter("")
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
