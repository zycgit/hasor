---
id: custom
sidebar_position: 6
title: 自定义拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 自定义拦截器

拦截器的匹配器
- 类型：`net.hasor.cobble.dynamic.Matchers`

匹配所有类
- `Matchers.anyClass();`

匹配所有方法
- `Matchers.anyMethod();`

匹配标记了 `@MyAop` 注解的类
- `Matchers.annotatedWithClass(MyAop.class);`

匹配标记了 `@MyAop` 注解的方法
- `Matchers.annotatedWithMethod(MyAop.class)`

匹配 `List` 类型的子类
- `Matchers.subClassesOf(List.class);`

按照通配符匹配类
- 格式为：`<包名>.<类名>`
- 通配符符号为：`?` 表示任意一个字符；`*`  表示任意多个字符。
- `Matchers.expressionClass("abc.foo.*");`

## 通配符匹配方法样例

```text
* *.*()                  匹配：任意无参方法
* *.*(*)                 匹配：任意方法
* *.add*(*)              匹配：任意add开头的方法
* *.add*(*,*)            匹配：任意add开头并且具有两个参数的方法。
* net.test.hasor.*(*)    匹配：包“net.test.hasor”下的任意类，任意方法。
* net.test.hasor.add*(*) 匹配：包“net.test.hasor”下的任意类，任意add开头的方法。
java.lang.String *.*(*)  匹配：任意返回值为String类型的方法。
```

## 自定义Aop注解

首先声明自己的注解。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MyAop {
}
```

其次编写拦截器

```java
public class SimpleInterceptor implements MethodInterceptor {
    public static boolean called = false;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        called = true;
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

最后，配置拦截器的筛选器。筛选所有标记了 MyAop 注解的 Bean 都使用我们的拦截器，我们在 Module 中进行如下声明：

```java
public class MyAopSetup implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //1.任意类
        Predicate<Class<?>> atClass = Matchers.anyClass();
        //2.有MyAop注解的方法
        Predicate<Method> atMethod = Matchers.annotatedWithMethod(MyAop.class);
        //3.让@MyAop注解生效
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}
```
