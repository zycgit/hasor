---
id: cache
sidebar_position: 3
title: c.上传缓存
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 上传缓存

在非流式上传中，大一点的文件在上传过程中都需要缓存上传数据。Hasor 可以在代码中处理上传时临时指定上传路径：

```java title='例子'
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

如果使用默认缓存路径来保存上传的临时数据那么程序代码可以简化为：

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

框架中默认缓存路径是 `${RUN_PATH}/temp/fragment`，这个路径可以通过下面这个配置改变：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <fileupload>
            <!-- 上传文件缓存目录 -->
            <cacheDirectory>${RUN_PATH}/temp/fragment</cacheDirectory>
        </fileupload>
    </hasor>
</config>
```

## 缓存配置

Hasor 在上传中可以配置的缓存信息有：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <fileupload>
            <!-- 上传文件缓存目录 -->
            <cacheDirectory>${RUN_PATH}/temp/fragment</cacheDirectory>
            <!-- 允许的请求大小 ( -1 表示不限制)-->
            <maxRequestSize>${HASOR_UPLOAD_MAX_REQUEST_SIZE:-1}</maxRequestSize>
            <!-- 允许上传的单个文件大小( -1 表示不限制) -->
            <maxFileSize>${HASOR_UPLOAD_MAX_FILE_SIZE:-1}</maxFileSize>
        </fileupload>
    </hasor>
</config>
```

其中占位符和值关系为：

| 占位符                            | 值                                            |
|-------------------------------|----------------------------------------------|
| RUN_PATH                      | 应用启动目录，Hasor 创建 Settings 时会写入该系统属性。        |
| HASOR_UPLOAD_MAX_REQUEST_SIZE | 允许的请求大小 ( `-1` 表示不限制)，默认为：`-1`               |
| HASOR_UPLOAD_MAX_FILE_SIZE    | 允许上传的单个文件大小( `-1` 表示不限制)，默认为：`-1`            |
