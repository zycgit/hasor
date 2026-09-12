---
id: stream
sidebar_position: 2
title: 流式上传
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 流式上传

流式文件上传，流式上传最大的好处就是不需要缓存就可以处理大文件的上传：

```java title='例子'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    @Any
    public void execute() throws IOException {
        Iterator<FileItemStream> multiStream = this.getMultipartIterator();
        while (multiStream.hasNext()) {
            FileItemStream next = multiStream.next();
            if (!next.getName().equals("xxxxx")) {
                continue;
            }
            InputStream inputStream = next.openStream();
            try {
                stream copy ...
            } finally {
                inputStream.close();
            }
        }
    }
}
```
