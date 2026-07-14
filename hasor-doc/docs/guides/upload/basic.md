---
id: basic
sidebar_position: 1
title: a.表单上传
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 表单上传

Hasor 内置了 apache 的 fileuplaod 组件，并且对该组件做了精简优化。因此在使用 Hasor 的文件上传时您无需也不会引入任何第三方 jar。

首先使用文件上传，必须通过 `WebController` 类进行操作，这里有文件上传例子：

```java title='例子'
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

而对应的 html 页面是一个普通的表单上传，但是要注意 form 标签的 enctype 属性要改成 `multipart/form-data`

```html
<form action="/fileupload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upfile"/>
    <input type="submit" value="上传"/>
</form>
```
