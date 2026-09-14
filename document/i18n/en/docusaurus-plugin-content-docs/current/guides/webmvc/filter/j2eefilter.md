---
id: j2eefilter
sidebar_position: 2
title: 4.6.2 Filter Form
description: Implement request interception with a traditional J2EE Filter.
---

# 4.6.2 Filter Form

A traditional J2EE `Filter` can act as an interceptor. For example:

```java title='Example'
public class MyFilter implements Filter {
    ...
}
```

Finally, declare and register the interceptor to use it normally.

```java title='Configure the interceptor'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.jeeFilter("/*").through(MyFilter.class);     // Filter form.
        ...
    }
}
```
