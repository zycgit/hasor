---
id: cache
sidebar_position: 3
title: 4.7.3 Upload Cache
description: Configure temporary cache storage for non-streaming uploads.
---

# 4.7.3 Upload Cache

In non-streaming uploads, larger files need cached upload data during the upload process. Hasor can temporarily specify an upload path in code while handling an upload:

```java title='Example'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    @Any
    public void execute() throws IOException {
        String cacheDirectory = "...";
        Integer maxPostSize = 1024 * 1024;
        List<FileItem> multipartList = this.getMultipart("upfile", cacheDirectory, maxPostSize);
    }
}
```

If you use the default cache path to store temporary upload data, the program code can be simplified as follows:

```java
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    @Any
    public void execute() throws IOException {
        for (FileItem multipart : this.getMultipart("upfile")) {
            try (FileOutputStream out = new FileOutputStream(new File("upload.bin"))) {
                multipart.writeTo(out);
            }
            multipart.deleteOrSkip();
        }
    }
}
```

The default cache path in the framework is `${RUN_PATH}/temp/fragment`. This path can be changed with the following configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <fileupload>
            <!-- Upload file cache directory. -->
            <cacheDirectory>${RUN_PATH}/temp/fragment</cacheDirectory>
        </fileupload>
    </hasor>
</config>
```

## Cache Configuration

The upload cache information configurable in Hasor is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <fileupload>
            <!-- Upload file cache directory. -->
            <cacheDirectory>${RUN_PATH}/temp/fragment</cacheDirectory>
            <!-- Allowed request size (-1 means unlimited). -->
            <maxRequestSize>${HASOR_UPLOAD_MAX_REQUEST_SIZE:-1}</maxRequestSize>
            <!-- Allowed size of a single uploaded file (-1 means unlimited). -->
            <maxFileSize>${HASOR_UPLOAD_MAX_FILE_SIZE:-1}</maxFileSize>
        </fileupload>
    </hasor>
</config>
```

The placeholder-value relationship is:

| Placeholder                   | Value                                                           |
|-------------------------------|-----------------------------------------------------------------|
| RUN_PATH                      | Application startup directory, written by Hasor when creating Settings. |
| HASOR_UPLOAD_MAX_REQUEST_SIZE | Allowed request size (`-1` means unlimited). Default: `-1`       |
| HASOR_UPLOAD_MAX_FILE_SIZE    | Allowed size of a single uploaded file (`-1` means unlimited). Default: `-1` |
