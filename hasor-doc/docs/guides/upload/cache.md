---
id: cache
sidebar_position: 3
title: c.上传缓存
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 上传缓存

在非流式上传中，大一点的文件在上传过程中都需要缓存上传数据。Hasor 可以在代码中处理上传时临时指定上传路径：

```java title='例子'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    public void execute() throws IOException {
        String cacheDirectory = "...";
        Integer maxPostSize = 1024 * 1024;
        FileItem multipart1 = this.getOneMultipart("upfile", cacheDirectory, maxPostSize);
    }
}
```

如果使用默认缓存路径来保存上传的临时数据那么程序代码可以简化为：

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

框架中默认缓存路径是 `%USER.HOME%/hasor-work/temp/fragment` 这个路径可以通过下面这个配置改变：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <fileupload>
            <!-- 上传文件缓存目录 -->
            <cacheDirectory>%WORK_HOME%/temp/fragment</cacheDirectory>
        </fileupload>
    </hasor>
</config>
```

## 缓存配置

Hasor 在上传中可以配置的缓存信息有：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <fileupload>
            <!-- 上传文件缓存目录 -->
            <cacheDirectory>%WORK_HOME%/temp/fragment</cacheDirectory>
            <!-- 允许的请求大小 ( -1 表示不限制)-->
            <maxRequestSize>${HASOR_UPLOAD_MAX_REQUEST_SIZE}</maxRequestSize>
            <!-- 允许上传的单个文件大小( -1 表示不限制) -->
            <maxFileSize>${HASOR_UPLOAD_MAX_FILE_SIZE}</maxFileSize>
        </fileupload>
    </hasor>
</config>
```

其中环境变量的值关系为：

| 环境变量                          | 值                                            |
|-------------------------------|----------------------------------------------|
| WORK_HOME                     | `%USER.HOME%/hasor-work`，其是在 hasor-core 中定义的 |
| USER.HOME                     | 系统环境变量，登录系统之后的用户主目录。例如：`/home/xxx/`          |
| HASOR_UPLOAD_MAX_REQUEST_SIZE | 允许的请求大小 ( `-1` 表示不限制)，默认为：`-1`               |
| HASOR_UPLOAD_MAX_FILE_SIZE    | 允许上传的单个文件大小( `-1` 表示不限制)，默认为：`-1`            |

:::tip
- 用户主目录如果是 linux 系统那么这个目录通常在这里：`“/home/xxx/hasor-work/temp/fragment”`
- 用户主目录如果是 window 用户住目录会在：`“c:/users/xxx/hasor-work/temp/fragment”`
:::