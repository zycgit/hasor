---
id: globallevel
sidebar_position: 4
title: c. Global Interceptors
description: Configure interceptors that match any class and any method.
---

# Global Interceptors

A global interceptor is effectively an interceptor that matches any class and any method. This kind of interceptor must be declared in a module:

```java title='Example'
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // 1. Any class.
        Matcher<Class<?>> atClass = Matchers.anyClass();
        // 2. Any method.
        Matcher<Method> atMethod = Matchers.anyMethod();
        // 3. Register the interceptor.
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}
```
