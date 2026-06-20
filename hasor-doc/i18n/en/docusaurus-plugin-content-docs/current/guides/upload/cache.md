---
id: cache
sidebar_position: 3
title: c. Upload Cache
description: Configure temporary cache storage for non-streaming uploads.
---

# Upload Cache

In non-streaming uploads, larger files need cached upload data during the upload process. Hasor can temporarily specify an upload path in code while handling an upload:

```java title='Example'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    public void execute() throws IOException {
        String cacheDirectory = "...";
        Integer maxPostSize = 1024 * 1024;
        FileItem multipart1 = this.getOneMultipart("upfile", cacheDirectory, maxPostSize);
    }
}
```

If you use the default cache path to store temporary upload data, the program code can be simplified as follows:

```java
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    public void execute() throws IOException {
        FileItem multipart = this.getOneMultipart("upfile");
        multipart.writeTo(new File(""));
        multipart.deleteOrSkip();
    }
}
```

The default cache path in the framework is `${user.home}/hasor-work/temp/fragment`. This path can be changed with the following configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <fileupload>
            <!-- Upload file cache directory. -->
            <cacheDirectory>${user.home}/hasor-work/temp/fragment</cacheDirectory>
        </fileupload>
    </hasor>
</config>
```

## Cache Configuration

The upload cache information configurable in Hasor is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <fileupload>
            <!-- Upload file cache directory. -->
            <cacheDirectory>${user.home}/hasor-work/temp/fragment</cacheDirectory>
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
| user.home                     | Java system property: the user's home directory after login, such as `/home/xxx/` |
| HASOR_UPLOAD_MAX_REQUEST_SIZE | Allowed request size (`-1` means unlimited). Default: `-1`       |
| HASOR_UPLOAD_MAX_FILE_SIZE    | Allowed size of a single uploaded file (`-1` means unlimited). Default: `-1` |

:::tip
- On Linux, the user home path is usually similar to `/home/xxx/hasor-work/temp/fragment`.
- On Windows, the user home path is usually similar to `c:/users/xxx/hasor-work/temp/fragment`.
:::
