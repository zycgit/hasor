/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.valid;
import net.hasor.test.web.actions.valid.ValidRequestFieldBean;
import net.hasor.web.valid.ValidInvoker;
import net.hasor.web.valid.Validation;

public class UnexpectedValid implements Validation<ValidRequestFieldBean> {
    @Override
    public void doValidation(String scene, ValidRequestFieldBean dataForm, ValidInvoker errors) {
        errors.addError("err1", "max out of %s"); // 丢失格式化参数
        errors.addError("err2", "max out of %d", "abc"); // 格式化错误
        //
        errors.addError("err3", "message 1");
        errors.addError("err3", "message 2");
        //
        try {
            errors.addError("", "abcdefg", "aaa");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("valid error message key is null.");
        }
    }
}
