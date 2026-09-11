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
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.codec.MD5;
import net.hasor.web.FileItem;
import net.hasor.web.WebController;
import net.hasor.web.annotation.Any;

public class FileItemUploadAction extends WebController {
    @Any
    public Map<String, String> execute() throws IOException, NoSuchAlgorithmException {
        Map<String, String> hashData = new HashMap<>();
        //
        List<FileItem> fileItems = getMultipartList();
        for (FileItem fileItem : fileItems) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fileItem.writeTo(byteArrayOutputStream);
            hashData.put(fileItem.getFieldName(), MD5.encodeMD5(byteArrayOutputStream.toByteArray()));
        }
        //
        return hashData;
    }
}
