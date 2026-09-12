---
id: defaults
sidebar_position: 0
title: Default return-value rendering
description: Configure built-in response rendering, encoding and redirects.
---

# Default return-value rendering

Rendering is built into Hasor Web, without Config, Boot or a rendering filter.
Objects and collections default to JSON, strings to text; null and void produce no default body.
Strings are not implicit template names. See [rendering engines](./render.md) for explicit views.

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

Rendering runs after the Action, before business filters unwind. Filter short-circuit return values are not rendered automatically.
Acquiring the response writer or output stream takes ownership; automatic rendering does not append to that response.
Committed, asynchronous, 204 and 304 responses are also skipped. HEAD retains calculated length but omits the body.
Static [resources](../web/resources.md) have a separate handling path.

## Redirects

`@net.hasor.web.render.RedirectTo` defaults to 302; `@RedirectTo(301)` explicitly changes the status.
Supported values are 301, 302, 303, 307 and 308. Method annotations override class annotations.
Return the destination; blank locations and CR/LF are rejected. Unsupported statuses fail during rendering.

## Migration

Move old Boot/Config rendering defaults to `hasor.render.defaults.objectEngine` and `stringEngine`.
Remove `RenderWebPlugin`: rendering is a built-in stage rather than a separately registered filter.
