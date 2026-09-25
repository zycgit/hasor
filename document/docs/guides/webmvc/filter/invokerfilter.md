---
id: invokerfilter
sidebar_position: 2
title: 4.6.1 InvokerFilter形式
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.6.1 InvokerFilter形式

`InvokerFilter` 按路径匹配，包裹已匹配 Action 的 MVC 调用、异常处理和渲染。静态资源和未匹配 Action 的请求不会进入这条过滤器链。
需要前置、后置和完成回调时，使用 [HandlerInterceptor](./interceptor.md)。

```java title='例子'
public class MyInvokerFilter implements InvokerFilter {
    public Object doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        try {
            // before
            Object result = chain.doNext(invoker);
            // after
            return result;
        } catch (Throwable e) {
            // error
            throw e;
        }
    }
}
```

最后对拦截器进行声明注册就可以正常使用了。

```java title='配置拦截器'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.filter("/*").through(MyInvokerFilter.class); // InvokerFilter形式
        ...
    }
}
```
