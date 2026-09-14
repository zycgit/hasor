---
id: stream
sidebar_position: 2
title: 4.7.2 Streaming Upload
description: Process large uploads without caching the whole file.
---

# 4.7.2 Streaming Upload

The biggest advantage of streaming file upload is that large files can be processed without caching:

```java title='Example'
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
