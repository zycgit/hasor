---
id: json
sidebar_position: 4
title: d.JSON渲染引擎
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# JSON 渲染引擎

Hasor Web 默认将对象和集合渲染为 JSON，字符串为文本，null 和 void 不产生默认正文。
无需在每个 Action 中设置渲染器、Content-Type 或过滤器。

```java
@Get
public java.util.Map<String, Object> hello() {
    return java.util.Map.of("message", "你好");
}
```

## JSON 库选择

`AutoJsonRenderEngine` 按 Jackson 2、Gson、Fastjson 1、Fastjson 2 的顺序选择应用类路径中的实现。
应用需提供相应库；默认 JSON 被选中而没有库时启动失败。已存在但损坏的实现会报错，不静默回退。
默认选择不主动查找容器中的 Gson 或 ObjectMapper Bean。

## 自定义序列化

通过 `WebMvcConfigurer.configureJson` 注册配置好的引擎：

```java
@Override
public void configureJson(net.hasor.config.web.render.JsonRenderConfigurer configurer) {
    com.google.gson.Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
    configurer.renderEngine(new net.hasor.web.render.json.GsonRenderEngine(gson));
}
```

其他实现支持：
- `JacksonRenderEngine`：ObjectMapper。
- `JsonRenderEngine`：Fastjson 1 的 SerializeConfig 和 SerializerFeature。
- `Fastjson2RenderEngine`：ObjectWriterProvider 和 JSONWriter.Feature，或 Supplier&lt;JSONWriter.Context&gt;。

上下文工厂每次返回独立实例；共享序列化对象应在处理请求前配置好，不在请求期间修改。
这些配置只影响响应序列化，不改变请求参数绑定。

`renderEngine("name")` 只设置后续注册的名称，单独调用不会切换默认引擎。
实例、类型和 Supplier 重载提供实际引擎；MVC 回调会完成注册，无需手动调用 `register()`。
默认选择由 `hasor.render.defaults.objectEngine` 和 `stringEngine` 控制。

完全自定义时，实现 RenderEngine，通过 `binder.addRender("json").to(CustomJsonRenderEngine.class)` 注册。
显式注册优先于配置中的同名后备实现。详见[默认渲染](./defaults.md)。
