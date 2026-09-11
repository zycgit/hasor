/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.actions.args;
import java.util.HashMap;
import java.util.Map;
import net.hasor.web.annotation.Any;
import net.hasor.web.annotation.QueryParameter;

public class QueryArgsAction {
    @Any
    public Map<String, Object> execute(//
            @QueryParameter("byteParam") byte byteParam, //
            @QueryParameter("intParam") int intParam,    //
            @QueryParameter("strParam") String strParam, //
            @QueryParameter("") String eptParam          //
    ) {
        return new HashMap<String, Object>() {{
            put("byteParam", byteParam);
            put("intParam", intParam);
            put("strParam", strParam);
            put("eptParam", eptParam);
        }};
    }
}
