/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.valid;
import java.util.HashMap;
import net.hasor.web.valid.ValidInvoker;

public class ValidTestUtils {
    public static HashMap<String, Object> newHashMap(ValidInvoker validInvoker, ValidRequestFieldBean fieldBean) {
        return new HashMap<String, Object>() {{
            put("byteParam", fieldBean.getByteParam());
            put("intParam", fieldBean.getIntParam());
            put("strParam", fieldBean.getStrParam());
            put("eptParam", fieldBean.getEptParam());
            //
            put("doValid", validInvoker.isValid());
            put("validErrorsOfString", validInvoker.validErrorsOfString());
            put("validErrorsOfMessage", validInvoker.validErrorsOfMessage());
        }};
    }
}
