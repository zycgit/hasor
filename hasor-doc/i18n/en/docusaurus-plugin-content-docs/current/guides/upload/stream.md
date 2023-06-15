---
id: stream
sidebar_position: 2
title: b.流式上传
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 流式上传

流式文件上传，流式上传最大的好处就是不需要缓存就可以处理大文件的上传：

```java title='例子'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
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
