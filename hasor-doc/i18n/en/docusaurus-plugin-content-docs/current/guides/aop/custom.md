---
id: custom
sidebar_position: 6
title: e. Custom Interceptors
description: Define custom AOP matchers and annotations.
---

# Custom Interceptors

Interceptor matcher:
- Type: `net.hasor.core.exts.aop.Matchers`

Match all classes:
- `Matchers.anyClass();`

Match all methods:
- `Matchers.anyMethod();`

Match classes annotated with `@MyAop`:
- `Matchers.annotatedWithClass(MyAop.class);`

Match methods annotated with `@MyAop`:
- `Matchers.annotatedWithMethod(MyAop.class)`

Match subclasses of `List`:
- `Matchers.subClassesOf(List.class);`

Match classes by wildcard:
- Format: `<package-name>.<class-name>`
- Wildcards: `?` means any single character; `*` means any number of characters.
- `Matchers.expressionClass("abc.foo.*");`

## Wildcard Method Matching Examples

```text
* *.*()                  Matches: any no-argument method.
* *.*(*)                 Matches: any method.
* *.add*(*)              Matches: any method whose name starts with add.
* *.add*(*,*)            Matches: any method whose name starts with add and has two parameters.
* net.test.hasor.*(*)    Matches: any method on any class under the net.test.hasor package.
* net.test.hasor.add*(*) Matches: any method whose name starts with add on any class under the net.test.hasor package.
java.lang.String *.*(*)  Matches: any method whose return type is String.
```

## Custom AOP Annotation

First, declare your own annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MyAop {
}
```

Second, write the interceptor.

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

Finally, configure the interceptor filter. To make our interceptor apply to all beans marked with the `MyAop` annotation, declare the following in a module:

```java
public class MyAopSetup implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // 1. Any class.
        Matcher<Class<?>> atClass = AopMatchers.anyClass();
        // 2. Methods annotated with MyAop.
        Matcher<Method> atMethod = AopMatchers.annotatedWithMethod(MyAop.class);
        // 3. Enable the @MyAop annotation.
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}
```
