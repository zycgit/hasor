/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.autoscan;

public class AutoScanMarker {
    private final String value;

    public AutoScanMarker(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
