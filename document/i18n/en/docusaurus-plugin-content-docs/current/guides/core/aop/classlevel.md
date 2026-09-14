---
id: classlevel
sidebar_position: 3
title: 2.5.2 Class-Level Interceptors
description: Configure AOP interceptors on all methods of a class.
---

# 2.5.2 Class-Level Interceptors

`hasor-core` processes `@Aop` through `net.hasor.core.aop.AopModule` by default, without a Config dependency. The annotations and interceptor interfaces are in `net.hasor.cobble.dynamic`; programmatic configuration is also available through `ApiBinder.bindInterceptor`.

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
