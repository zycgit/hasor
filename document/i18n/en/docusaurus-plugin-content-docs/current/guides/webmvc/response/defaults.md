---
id: defaults
sidebar_position: 0
title: 4.4.1 Default return-value rendering
description: Configure built-in response rendering, encoding and redirects.
---

# 4.4.1 Default return-value rendering

Rendering is built into Hasor Web, without Config, Boot or a rendering filter.
Objects and collections default to JSON, strings to text.
Strings are not implicit template names. See [rendering engines](./render.md) for explicit views.

## Invocation and rendering order

Starting with **5.3.0**, MVC completes invocation and exception handling first, then checks `isSkipRender()` before rendering the final result.
Normal return values, results replaced by `postHandle`, and exception handler results use the same rendering path. A `void` method declaration does not discard a result supplied later.

The following assumes a synchronous request, default renderers, and a response that has not been committed or taken over by the application:

![MVC invocation and exception handling produce the final result, then SkipRender determines whether it is rendered. Unresolved and rendering failures propagate outward.](/img/webmvc-result-flow-en.svg)

The diagram omits the short-circuit branch where `preHandle` returns `false`; see [MVC interceptors](../filter/interceptor.md) for callback ordering.
A normal null or void return has no default body when post-handlers supply no result. `false`, zero, and empty strings are valid results.
An explicitly selected renderer or view may still process a null result; call `setSkipRender()` to skip output completely.
See [exception handling](./exception.md) for exception result rules.

## Default engines

Defaults belong to `META-INF/hasor-framework/web-hconfig.xml`. Override them in application hconfig:

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

Built-in names are `json`, `text`, and `none`. Their implementations are
`net.hasor.web.render.json.AutoJsonRenderEngine`, `net.hasor.web.render.text.TextRenderEngine`,
and `net.hasor.web.render.none.NoopRenderEngine`.
Choose `none` to disable implicit output for that return-value category; explicit rendering still applies.
Environment variables are `HASOR_WEB_RENDER_OBJECT_TYPE` and `HASOR_WEB_RENDER_STRING_TYPE`.
JSON requires an application-supplied library; see [JSON rendering](./json_render.md).

## Custom engines

Use `binder.addRender("custom").to(CustomRenderEngine.class)` and select that name in the defaults.
Explicit bindings override same-name configuration fallbacks. Alternatively, configure:

```xml
<hasor.render>
    <engines>
        <engine name="text">example.CustomTextRenderEngine</engine>
    </engines>
</hasor.render>
```

A configured class needs a public no-argument or ClassLoader constructor; constructor injection is not automatic in this path.
Use Binder registration for container-managed engines. Unknown default names and invalid implementations fail startup.

## Encoding and response ownership

Use `binder.setEncodingCharacter("UTF-8", "UTF-8")` for request and response encoding.
If no global response encoding is set, use the request encoding. If neither is set, retain the Servlet default.
An Action can override encoding before writing through `@Produces` or the response API.
There is no separate rendering charset setting or default-encoding API.

Rendering runs after the Action and MVC `postHandle`, before `afterCompletion` and outer filters unwind. Filter short-circuit return values are not rendered automatically.
Declaring a `ServletResponse` or `HttpServletResponse` parameter does not automatically skip rendering. An explicitly selected renderer may still run for a void or null return; call `setSkipRender()` explicitly when handling the response yourself.
Setting headers through a response parameter and then returning a non-null value still renders that value.
Acquiring the response writer or output stream takes ownership; automatic rendering does not append to that response.
Committed responses, requests in Servlet asynchronous mode, and 204/304 responses skip automatic rendering. This currently also applies to framework `@Async` requests; see [asynchronous requests](../j2ee/async.md). HEAD computes content length without writing a body.
Static [resources](../web/resources.md) have a separate handling path.

Controllers and MVC interceptors can explicitly skip rendering through `Invoker`:

```java
@Override
public void postHandle(Invoker invoker, Object result) {
    invoker.getHttpResponse().setStatus(204);
    invoker.setSkipRender();
}
```

`isSkipRender()` starts as `false`. `setSkipRender()` takes no arguments and is idempotent: it enables skipping further rendering and cannot re-enable rendering.
The state belongs to the current request and is shared by `Invoker` wrappers and framework asynchronous calls. Each subsequent request starts with `false`.
Layout and render-type defaults are initialized before invocation, and Controllers and interceptors can override them. After MVC invocation and exception handling produce the final result, `InvokerCaller` checks `isSkipRender()` before passing that result to `RenderProcessor` for output.
Normal return values and exception handler return values use the same rendering path. Setting this state does not stop Controllers, interceptor callbacks, or exception handling; a pre-handler must return `false` to stop MVC execution.
Set the state before rendering begins. Renderers only produce output and must not use `setSkipRender()` to control an ongoing render; the framework does not interrupt rendering based on changes to that state.

## Redirects

`@net.hasor.web.render.RedirectTo` defaults to 302; `@RedirectTo(301)` explicitly changes the status.
Supported values are 301, 302, 303, 307 and 308. Method annotations override class annotations.
Return the destination; blank locations and CR/LF are rejected. Unsupported statuses fail during rendering.

## Migration

Move old Boot/Config rendering defaults to `hasor.render.defaults.objectEngine` and `stringEngine`.
Remove `RenderWebPlugin`: rendering is a built-in stage rather than a separately registered filter.
