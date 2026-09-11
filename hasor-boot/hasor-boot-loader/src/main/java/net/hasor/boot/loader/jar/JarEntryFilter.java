/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
/**
 * Interface that can be used to filter and optionally rename jar entries.
 * @author Phillip Webb
 */
interface JarEntryFilter {
    /**
     * Apply the jar entry filter.
     * @param name the current entry name. This may be different that the original entry
     * name if a previous filter has been applied
     * @return the new name of the entry or {@code null} if the entry should not be
     * included.
     */
    AsciiBytes apply(AsciiBytes name);
}
