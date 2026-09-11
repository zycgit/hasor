/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.upload.factorys.disk;
import java.io.*;
import net.hasor.cobble.io.IOUtils;
import net.hasor.web.FileItemStream;
import net.hasor.web.upload.FileItemBase;
import net.hasor.web.upload.util.DeferredFileOutputStream;

/**
 * 磁盘缓存,50KB以内的数据在内存中驻留,超过50KB的数据全部走磁盘缓存。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-08-31
 */
public class DiskFileItem extends FileItemBase {
    public static final int                      DEFAULT_SIZE_THRESHOLD = 51200;// 50KB
    private             DeferredFileOutputStream dfos;
    private             File                     cacheFile;

    public DiskFileItem(FileItemStream stream, File cacheFile) throws IOException {
        super(stream);
        this.init(stream, cacheFile);
    }

    protected void init(FileItemStream stream, File cacheFile) throws IOException {
        this.cacheFile = cacheFile;
        File parent = cacheFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        this.dfos = new DeferredFileOutputStream(DEFAULT_SIZE_THRESHOLD, cacheFile);
        IOUtils.copy(stream.openStream(), this.dfos);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.deleteOrSkip();
        } finally {
            super.finalize();
        }
    }

    @Override
    public long getSize() {
        if (this.dfos.isInMemory()) {
            return this.dfos.getData().length;
        } else {
            return this.dfos.getFile().length();
        }
    }

    @Override
    public void deleteOrSkip() {
        try {
            if (this.cacheFile != null && this.cacheFile.exists()) {
                this.cacheFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InputStream openStream() throws IOException {
        if (this.dfos.isInMemory()) {
            return new ByteArrayInputStream(this.dfos.getData());
        } else {
            return new FileInputStream(this.dfos.getFile());
        }
    }
}
