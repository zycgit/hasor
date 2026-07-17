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

When using the JSON renderer, the request method sets the renderer to `json`. The returned object is stored under `Invoker.RETURN_DATA_KEY` so the renderer can serialize it.

```java
@MappingTo("/helloAction.json")
public class HelloAction {
    @Any
    public Object execute(RenderInvoker invoker) {
        invoker.renderType("json");
        return ...
    }
}
```

You can further use `InvokerFilter` to handle renderer selection uniformly:

```java
public class UseJsonInvokerFilter implements InvokerFilter {
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        ((RenderInvoker)invoker).renderType("json");
        return chain.doNext(invoker);
    }
}

public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        apiBinder.filter("*.json").through(UseJsonInvokerFilter.class);
    }
}
```
