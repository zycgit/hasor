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
 * Callback visitor triggered by {@link CentralDirectoryParser}.
 * @author Phillip Webb
 */
interface CentralDirectoryVisitor {
    void visitStart(CentralDirectoryEndRecord endRecord, RandomFile centralDirectoryData);

    void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset);

    void visitEnd();
}
