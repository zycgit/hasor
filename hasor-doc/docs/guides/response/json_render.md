---
id: json
sidebar_position: 4
title: d.JSON渲染引擎
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# JSON渲染引擎

```java
/**
 * 使用 FastJson 作为序列化工具的 Json 渲染器
 * @version : 2016年1月3日
 * @author 赵永春 (zyc@hasor.net)
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

使用 Json 渲染器时，请求方法需要把渲染器设置为 `json`。返回值会被放入 `Invoker.RETURN_DATA_KEY`，供渲染器序列化输出。

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

进一步还可以利用 `InvokerFilter` 把设置渲染器的工作统一处理：

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
