/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.valid;
import net.hasor.web.Invoker;
import net.hasor.web.InvokerCreator;

/**
 * 表单验证器，Invoker扩展。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class ValidInvokerCreator implements InvokerCreator {
    @Override
    public Invoker createExt(Invoker dataContext) {
        ValidInvokerSupplier supplier = new ValidInvokerSupplier(dataContext);
        supplier.put(ValidInvoker.VALID_DATA_KEY, supplier.getValidData());
        supplier.lockKey(ValidInvoker.VALID_DATA_KEY);
        return supplier;
    }
}
