---
id: json
sidebar_position: 4
title: d. JSON Rendering Engine
description: JSON library selection and custom response serialization in Hasor Web.
---

# JSON Rendering Engine

Hasor Web renders objects and collections as JSON and strings as text by default. null and void do not produce a default body.
There is no need to configure a renderer, Content-Type, or filter in every Action.

```java
@Get
public java.util.Map<String, Object> hello() {
    return java.util.Map.of("message", "Hello");
}
```

## JSON library selection

`AutoJsonRenderEngine` searches the application classpath in this order: Jackson 2, Gson, Fastjson 1, then Fastjson 2.
The application must supply the library. Startup fails if default JSON rendering is selected but no library is available. A present but broken implementation reports an error rather than silently falling back.
Default selection does not look up Gson or ObjectMapper Beans in the container.

## Custom serialization

Register a configured engine through `WebMvcConfigurer.configureJson`:

```java
@Override
public void configureJson(net.hasor.config.web.render.JsonRenderConfigurer configurer) {
    com.google.gson.Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
    configurer.renderEngine(new net.hasor.web.render.json.GsonRenderEngine(gson));
}
```

Other implementations support:
- `JacksonRenderEngine`: ObjectMapper。
- `JsonRenderEngine`: Fastjson 1 SerializeConfig and SerializerFeature.
- `Fastjson2RenderEngine`: ObjectWriterProvider and JSONWriter.Feature, or Supplier&lt;JSONWriter.Context&gt;.

Context factories must return a separate instance on each call. Configure shared serialization objects before handling requests, and do not modify them during requests.
These settings affect response serialization only, not request parameter binding.

`renderEngine("name")` only sets the name for subsequent registration; calling it alone does not switch the default engine.
Instance, type, and Supplier overloads supply the actual engine. The MVC callback completes registration; do not call `register()` manually.
Default selection is controlled by `hasor.render.defaults.objectEngine` and `stringEngine`.

For a fully custom implementation, implement RenderEngine and register it through `binder.addRender("json").to(CustomJsonRenderEngine.class)`.
Explicit registration overrides a configured fallback with the same name. See [Default Rendering](./defaults.md).
