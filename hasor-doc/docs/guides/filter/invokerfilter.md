---
id: invokerfilter
sidebar_position: 2
title: a.InvokerFilter形式
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# InvokerFilter形式

```java title='例子'
public class MyInvokerFilter implements InvokerFilter {
    public void doInvoke(Invoker invoker, InvokerChain chain) throws Throwable {
        try {
            // before
            chain.doNext(invoker);
            // after
        } catch (Throwable e) {
            // error
            throw e;
        }
    }
}
```

最后对拦截器进行声明注册就可以正常使用了。

```java title='配置拦截器'
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.filter("/*").through(MyInvokerFilter.class); // InvokerFilter形式
        ...
    }
}
```
