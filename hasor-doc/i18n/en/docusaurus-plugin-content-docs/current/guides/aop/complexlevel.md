---
id: complexlevel
sidebar_position: 5
title: d. Composite Interceptors
description: Combine multiple AOP interceptors on the same or different join points.
---

# Composite Interceptors

Composite interceptors have two meanings:
- Multiple interceptors can intercept the same join point at the same time.
- Multiple interceptors can take effect at different locations at the same time.

First, multiple interceptors can intercept one join point. Their effective order is A first, then B. The code is shown below:

```java title='Example'
@Aop({SimpleInterceptorA.class, SimpleInterceptorB.class })
public class AopBean {
    ...
}
```

Second, multiple interceptors can take effect at different locations:

```java
@Aop(ClassInterceptor.class)
public class AopBean {
    public String print() {
        ...
    }

    @Aop(MethodInterceptor.class)
    public String echo(String sayMessage) {
        return "echo :" + sayMessage;
    }
}
```

In this case, `ClassInterceptor` takes effect when `print` is called. When `echo` is called, both `ClassInterceptor` and `MethodInterceptor` take effect, in the order class first, then method.

If a global AOP interceptor is also configured, the effective order is: `global level` -> `class level` -> `method level`.
