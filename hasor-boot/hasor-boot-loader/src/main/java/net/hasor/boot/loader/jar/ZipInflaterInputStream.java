/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * {@link InflaterInputStream} that supports the writing of an extra "dummy" byte (which
 * is required with JDK 6) and returns accurate available() results.
 * @author Phillip Webb
 */
class ZipInflaterInputStream extends InflaterInputStream {
    private int     available;
    private boolean extraBytesWritten;

    ZipInflaterInputStream(InputStream inputStream, int size) {
        super(inputStream, new Inflater(true), getInflaterBufferSize(size));
        this.available = size;
    }

    @Override
    public int available() throws IOException {
        if (this.available < 0) {
            return super.available();
        }
        return this.available;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            this.available -= result;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.inf.end();
    }

    @Override
    protected void fill() throws IOException {
        try {
            super.fill();
        } catch (EOFException ex) {
            if (this.extraBytesWritten) {
                throw ex;
            }
            this.len = 1;
            this.buf[0] = 0x0;
            this.extraBytesWritten = true;
            this.inf.setInput(this.buf, 0, this.len);
        }
    }

    private static int getInflaterBufferSize(long size) {
        size += 2; // inflater likes some space
        size = (size > 65536) ? 8192 : size;
        size = (size <= 0) ? 4096 : size;
        return (int) size;
    }
}
