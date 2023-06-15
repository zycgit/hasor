---
id: basic
sidebar_position: 1
title: a.表单上传
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 表单上传

Hasor 内置了 apache 的 fileuplaod 组件，并且对该组件做了精简优化。因此在使用 Hasor 的文件上传时您无需也不会引入任何第三方 jar。

首先使用文件上传，必须通过 `WebController` 类进行操作，这里有文件上传例子：

```java title='例子'
@MappingTo("/fileupload.do")
public class FileUpLoad extends WebController {
    public void execute() throws IOException {
        FileItem multipart = this.getOneMultipart("upfile");
        multipart.writeTo(...);
        multipart.deleteOrSkip();
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
