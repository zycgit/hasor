---
id: basic
sidebar_position: 1
title: Form Upload
description: Upload files from forms with Hasor Web.
---

# Form Upload

Hasor includes Apache FileUpload internally and simplifies and optimizes it. Therefore, when using Hasor file upload, you do not need to introduce any third-party jar.

To use file upload, you must operate through the `WebController` class. The following is a file upload example:

```java title='Example'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    @Any
    public void execute() throws IOException {
        for (FileItem multipart : this.getMultipart("upfile")) {
            multipart.writeTo(...);
            multipart.deleteOrSkip();
        }
    }
}
```

The corresponding HTML page is an ordinary upload form, but note that the `enctype` attribute of the `form` tag must be changed to `multipart/form-data`.

```html
<form action="/fileupload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upfile"/>
    <input type="submit" value="Upload"/>
</form>
```
