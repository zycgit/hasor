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
import java.util.UUID;
import net.hasor.cobble.io.IOUtils;
import net.hasor.web.FileItem;
import net.hasor.web.FileItemFactory;
import net.hasor.web.FileItemStream;
import net.hasor.web.upload.FileItemBase;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年2月11日
 */
public class DiskFileItemFactory implements FileItemFactory {
    private File cacheDirectory;

    public DiskFileItemFactory() {
    }

    public DiskFileItemFactory(String cacheDirectory) {
        this.cacheDirectory = new File(cacheDirectory);
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public FileItem createItem(FileItemStream itemStream) throws IOException {
        String fid = UUID.randomUUID() + ".tmp";
        if (itemStream.isFormField()) {
            return new MemoryFileItem(itemStream);
        } else {
            return createDiskFileItem(itemStream, fid);
        }
    }

    protected FileItem createDiskFileItem(FileItemStream itemStream, String fid) throws IOException {
        return new DiskFileItem(itemStream, new File(cacheDirectory, fid));
    }

    public static class MemoryFileItem extends FileItemBase {
        private final byte[] cachedContent;

        public MemoryFileItem(FileItemStream stream) throws IOException {
            super(stream);
            ByteArrayOutputStream arrays = new ByteArrayOutputStream();
            IOUtils.copy(stream.openStream(), arrays);
            this.cachedContent = arrays.toByteArray();
        }

        @Override
        public long getSize() {
            if (this.cachedContent != null) {
                return this.cachedContent.length;
            } else {
                return 0;
            }
        }

        @Override
        public void deleteOrSkip() {
        }

        @Override
        public InputStream openStream() throws IOException {
            if (this.cachedContent == null) {
                return null;
            }
            return new ByteArrayInputStream(this.cachedContent);
        }
    }
}
