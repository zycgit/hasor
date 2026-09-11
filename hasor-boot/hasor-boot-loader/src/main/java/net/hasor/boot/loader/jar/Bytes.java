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
 * Utilities for dealing with bytes from ZIP files.
 * @author Phillip Webb
 */
final class Bytes {
    private Bytes() {
    }

    static long littleEndianValue(byte[] bytes, int offset, int length) {
        long value = 0;
        for (int i = length - 1; i >= 0; i--) {
            value = ((value << 8) | (bytes[offset + i] & 0xFF));
        }
        return value;
    }
}
