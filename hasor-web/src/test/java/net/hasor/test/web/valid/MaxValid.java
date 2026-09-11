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

public class MaxValid implements Validation<ValidRequestFieldBean> {
    @Override
    public void doValidation(String scene, ValidRequestFieldBean dataForm, ValidInvoker errors) {
        if (dataForm.getByteParam() > 10) {
            errors.addError("byteParam", "max out of 10");
        }
        if (dataForm.getIntParam() > 10) {
            errors.addError("intParam", "max out of %s", 10);
        }
        //
        try {
            errors.addError("", "test message");
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("valid error message key is null.");
        }
        //
    }
}
