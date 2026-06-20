---
id: json
sidebar_position: 4
title: d. JSON Rendering Engine
description: Implement and use a JSON rendering engine.
---

# JSON Rendering Engine

```java
/**
 * JSON renderer using FastJson as the serialization tool.
 * @version : January 3, 2016
 * @author Yongchun Zhao (zyc@hasor.net)
 */
@Render("json")
public class JsonRender implements RenderEngine {
    public boolean exist(String template) throws IOException {
        return true;
    }

    public void process(RenderInvoker renderData, Writer writer) throws Throwable {
        Object data = renderData.get(Invoker.RETURN_DATA_KEY);
        JSON.writeJSONString(writer, data);
    }
}
```

When using the JSON renderer, the object returned by the `execute` method is automatically serialized by the JSON renderer and output to the frontend, while `ContentType` is set.

```java
@MappingTo("/helloAction.json")
public class HelloAction {
    @Produces("json")
    public Object execute(RenderInvoker invoker) {
        invoker.renderType("json");
        return ...
    }
}
```

You can further use `InvokerFilter` to handle renderer selection uniformly:

```java
@MappingTo("/helloAction.json")
public class UseJsonInvokerFilter implements InvokerFilter {
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        ((RenderInvoker)invoker).renderType("json");
        return chain.doNext(invoker);
    }
}
```
