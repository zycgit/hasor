/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.util.zip.ZipEntry;

/**
 * A file header record that has been loaded from a Jar file.
 * @author Phillip Webb
 * @see JarEntry
 * @see CentralDirectoryFileHeader
 */
interface FileHeader {
    /**
     * Returns {@code true} if the header has the given name.
     * @param name the name to test
     * @param suffix an additional suffix (or {@code 0})
     * @return {@code true} if the header has the given name
     */
    boolean hasName(CharSequence name, char suffix);

    /**
     * Return the offset of the load file header within the archive data.
     * @return the local header offset
     */
    long getLocalHeaderOffset();

    /**
     * Return the compressed size of the entry.
     * @return the compressed size.
     */
    long getCompressedSize();

    /**
     * Return the uncompressed size of the entry.
     * @return the uncompressed size.
     */
    long getSize();

    /**
     * Return the method used to compress the data.
     * @return the zip compression method
     * @see ZipEntry#STORED
     * @see ZipEntry#DEFLATED
     */
    int getMethod();
}
