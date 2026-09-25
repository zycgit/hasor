---
id: invokerfilter
sidebar_position: 2
title: 4.6.1 InvokerFilter Form
description: Implement request interception with InvokerFilter.
---

# 4.6.1 InvokerFilter Form

`InvokerFilter` matches paths and surrounds MVC invocation, exception handling, and rendering for matched Actions. Static resources and requests without a matched Action bypass this filter chain.
Use [HandlerInterceptor](./interceptor.md) for pre-handler, post-handler, and completion callbacks.

```java title='Example'
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

Finally, declare and register the interceptor to use it normally.

```java title='Configure the interceptor'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.filter("/*").through(MyInvokerFilter.class); // InvokerFilter form.
        ...
    }
}
```
