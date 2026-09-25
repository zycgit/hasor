---
id: defaults
sidebar_position: 0
title: 4.4.1 默认返回值渲染
description: 内置返回值渲染、编码、自定义引擎和重定向。
---

# 4.4.1 默认返回值渲染

返回值渲染是 Hasor Web 内置机制，不需要 Config、Boot 或渲染过滤器。
对象与集合默认 JSON，字符串默认文本。字符串不是隐式模板名，模板需显式指定，见[渲染器](./render.md)。

## 调用与渲染顺序

从 **5.3.0** 开始，MVC 先完成调用和异常处理，得到最终结果，再判断 `isSkipRender()`，决定是否进入渲染。
正常返回值、`postHandle` 修改后的结果和异常处理器返回值使用同一条渲染路径，原方法声明为 `void` 不会丢弃后续产生的结果。

以下以同步请求、默认渲染器、响应尚未提交且未被接管为前提：

![MVC 调用和异常处理汇合为最终结果，随后根据 SkipRender 决定是否渲染；未处理异常和渲染异常向外抛出。](/img/webmvc-result-flow-zh-cn.svg)

图中省略了 `preHandle` 返回 `false` 的中断分支，具体回调顺序见 [MVC 拦截器](../filter/interceptor.md)。
正常返回 `null` 或 `void`，且后置处理没有提供结果时，没有默认正文。`false`、`0` 和空字符串都是有效结果。
显式选择的渲染器或视图仍可处理 `null`，需要完全跳过输出时应调用 `setSkipRender()`。
异常结果的处理规则见[异常处理](./exception.md)。

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

渲染在 Action 和 MVC `postHandle` 之后、`afterCompletion` 和外层过滤器返回之前执行。过滤器短路返回值不会自动渲染。
应用取得响应 Writer 或输出流后视为接管正文，框架不再追加默认输出。
仅声明 `ServletResponse` 或 `HttpServletResponse` 参数不会自动跳过渲染。即使方法返回 `void` 或 `null`，显式选择的渲染器仍可执行；需要自行响应时应明确调用 `setSkipRender()`。
仅通过响应参数设置响应头、随后返回非空数据时，仍会正常渲染返回值。
已提交、处于 Servlet 异步状态、204、304 响应跳过自动渲染。当前 `@Async` 请求也受此规则约束，详见[异步请求](../j2ee/async.md)。
HEAD 保留计算的长度但不输出正文。
静态资源走独立的[资源处理](../web/resources.md)，不进入渲染阶段。

Controller 和 MVC 拦截器可通过 `Invoker` 显式跳过渲染：

```java
@Override
public void postHandle(Invoker invoker, Object result) {
    invoker.getHttpResponse().setStatus(204);
    invoker.setSkipRender();
}
```

`isSkipRender()` 初始为 `false`。`setSkipRender()` 不接收参数，重复调用无副作用，只能开启跳过渲染，不能恢复渲染。
状态保存在当前请求中，`Invoker` 包装对象与框架异步调用共享该状态；后续请求重新从 `false` 开始。
布局和渲染类型的默认值在调用前初始化，Controller 和拦截器仍可覆盖它们。MVC 调用及异常处理完成、最终结果确定后，`InvokerCaller` 才检查 `isSkipRender()`，决定是否将结果交给 `RenderProcessor` 输出。
普通返回值与异常处理器返回值使用同一条渲染路径。设置该状态不会终止 Controller、拦截器回调或异常处理；前置拦截器需要终止 MVC 时应返回 `false`。
应在渲染开始前设置该状态。渲染器只负责输出结果，不应在渲染过程中调用 `setSkipRender()`；`RenderProcessor` 不再读取 `isSkipRender()`，也不据此中断已经开始的渲染。

## 重定向

`@net.hasor.web.render.RedirectTo` 默认为 302，可写 `@RedirectTo(301)`；支持 301、302、303、307、308。
方法注解优先于类注解，返回值提供目标地址。空地址、CR/LF 和不支持的状态码会被拒绝。

## 迁移

旧 Boot/Config 渲染规则改用 `hasor.render.defaults.objectEngine`、`stringEngine`。
删除旧 `RenderWebPlugin` 装配；渲染不再是可以排序的业务过滤器。
