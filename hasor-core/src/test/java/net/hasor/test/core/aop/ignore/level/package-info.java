/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
/** 这个包不忽略 */
@Noise @IgnoreProxy(ignore = false, propagate = false)
package net.hasor.test.core.aop.ignore.level;
import net.hasor.core.IgnoreProxy;
import net.hasor.test.core.basic.inject.jsr330.Noise;
