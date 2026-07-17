---
id: classlevel
sidebar_position: 3
title: b.类级拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 类级拦截器

在 Hasor 中为 Bean 配置拦截器只需要一个注解即可，被标注类的所有方法就都被拦截了。

```java title='例如'
@Aop(SimpleInterceptor.class)
public class AopBean {
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
