/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.upload;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.hasor.cobble.codec.MD5;
import net.hasor.cobble.io.IOUtils;
import net.hasor.web.FileItemStream;
import net.hasor.web.WebController;
import net.hasor.web.annotation.Any;

public class FileItemStreamUploadAction extends WebController {
    @Any
    public Map<String, String> execute() throws IOException, NoSuchAlgorithmException {
        Map<String, String> hashData = new HashMap<>();
        //
        Iterator<FileItemStream> fileItems = getMultipartIterator();
        while (fileItems.hasNext()) {
            FileItemStream fileItem = fileItems.next();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = fileItem.openStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            //
            hashData.put(fileItem.getFieldName(), MD5.encodeMD5(byteArrayOutputStream.toByteArray()));
        }
        //
        return hashData;
    }
}
