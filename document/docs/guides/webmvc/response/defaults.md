---
id: defaults
sidebar_position: 0
title: 默认返回值渲染
description: 内置返回值渲染、编码、自定义引擎和重定向。
---

# 默认返回值渲染

返回值渲染是 Hasor Web 内置机制，不需要 Config、Boot 或渲染过滤器。
对象与集合默认 JSON，字符串默认文本，null 和 void 不输出默认正文。字符串不是隐式模板名，模板需显式指定，见[渲染器](./render.md)。

## 默认规则

默认值由 `META-INF/hasor-framework/web-hconfig.xml` 提供，应用 hconfig 可覆盖：

```xml
<config xmlns="https://www.hasor.net/sechma/main">
    <hasor.render>
        <defaults>
            <objectEngine>json</objectEngine>
            <stringEngine>text</stringEngine>
        </defaults>
    </hasor.render>
</config>
```

内置名称为 `json`、`text`、`none`，分别对应 `net.hasor.web.render.json.AutoJsonRenderEngine`、
`net.hasor.web.render.text.TextRenderEngine`、`net.hasor.web.render.none.NoopRenderEngine`。
设为 `none` 可关闭某类返回值的默认输出，但不影响显式选择渲染器。
环境变量为 `HASOR_WEB_RENDER_OBJECT_TYPE`、`HASOR_WEB_RENDER_STRING_TYPE`。
JSON 库需由应用提供，详见 [JSON 渲染](./json_render.md)。

## 自定义引擎

通过 `binder.addRender("custom").to(CustomRenderEngine.class)` 注册，然后在 defaults 中选择名称。
显式绑定覆盖配置中的同名后备实现。也可在配置中声明：

```xml
<hasor.render>
    <engines>
        <engine name="text">example.CustomTextRenderEngine</engine>
    </engines>
</hasor.render>
```

配置类需要公开无参或 ClassLoader 构造方法；该路径不自动进行构造注入，需要容器管理时使用 Binder。
未知默认引擎名或无效实现会导致启动失败。

## 编码与手动输出

`binder.setEncodingCharacter("UTF-8", "UTF-8")` 设置请求和响应编码。
未设置全局响应编码时取请求编码；均未设置时保留 Servlet 默认值。Action 可在写入前通过 `@Produces` 或响应 API 覆盖。
没有独立的渲染 charset 或默认编码 API。

渲染在 Action 返回后、业务过滤器后置逻辑前执行。过滤器短路返回值不会自动渲染。
应用取得响应 Writer 或输出流后视为接管正文，框架不再追加默认输出。
已提交、异步、204、304 响应也跳过自动渲染；HEAD 保留计算的长度但不输出正文。
静态资源走独立的[资源处理](../web/resources.md)，不进入渲染阶段。

## 重定向

`@net.hasor.web.render.RedirectTo` 默认为 302，可写 `@RedirectTo(301)`；支持 301、302、303、307、308。
方法注解优先于类注解，返回值提供目标地址。空地址、CR/LF 和不支持的状态码会被拒绝。

## 迁移

旧 Boot/Config 渲染规则改用 `hasor.render.defaults.objectEngine`、`stringEngine`。
删除旧 `RenderWebPlugin` 装配；渲染不再是可以排序的业务过滤器。
