---
id: methodlevel
sidebar_position: 2
title: 2.5.1 方法级拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.5.1 方法级拦截器

以下注解由 hasor-core 内置 AopModule 处理，不需要 hasor-config。`@Aop` 为 `net.hasor.cobble.dynamic.Aop`，对应的拦截器接口也位于 `net.hasor.cobble.dynamic` 包。

在某个类中只有某些特定的方法需要被拦截，那么就要使用方法级拦截器。

```java title='例如'
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

```java title='定义方法拦截器'
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

```java title='创建 Aop Bean'
AppContext appContext = Hasor.create().build();
appContext.getInstance(AopBean.class).echo("sss");
```
