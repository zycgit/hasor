---
id: methodlevel
sidebar_position: 2
title: a. Method-Level Interceptors
description: Configure AOP interceptors on specific methods.
---

# Method-Level Interceptors

When only certain methods in a class need interception, use method-level interceptors.

```java title='Example'
public class AopBean {
    public String print() {
        ...
    }

    @Aop(SimpleInterceptor.class)
    public String echo(String sayMessage) {
        return "echo :" + sayMessage;
    }
}
```

```java title='Define a method interceptor'
public class SimpleInterceptor implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            System.out.println("before... ");
            Object returnData = invocation.proceed();
            System.out.println("after...");
            return returnData;
        } catch (Exception e) {
            System.out.println("throw...");
            throw e;
        }
    }
}
```

```java title='Create an AOP bean'
AppContext appContext = Hasor.create().build();
appContext.getInstance(AopBean.class).echo("sss");
```
