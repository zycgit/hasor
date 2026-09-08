---
id: classlevel
sidebar_position: 3
title: b. Class-Level Interceptors
description: Configure AOP interceptors on all methods of a class.
---

# Class-Level Interceptors

In Hasor, configuring an interceptor for a bean only requires one annotation. All methods of the annotated class are intercepted.

```java title='Example'
@Aop(SimpleInterceptor.class)
public class AopBean {
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

## Required Module

Annotation AOP requires `hasor-config`, which installs `AopModule` by default. `Aop`, `MethodInterceptor`, and `MethodInvocation` are in `net.hasor.cobble.dynamic`. With core alone, configure interceptors explicitly through `ApiBinder.bindInterceptor`.
