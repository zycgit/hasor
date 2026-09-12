---
id: invokerfilter
sidebar_position: 2
title: InvokerFilter Form
description: Implement request interception with InvokerFilter.
---

# InvokerFilter Form

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
