---
id: ignore
sidebar_position: 7
title: f.忽略动态代理
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 忽略动态代理

## 标记到类上

任何类或者包上都可以通过标记 `@IgnoreProxy` 注解来达到不进行动态代理的目的。

```java
@IgnoreProxy
public class CoreService {
    ....
}
```

此时在通过 AppContext 的方法创建这个 Bean 对象时候，无论我们是否配置了什么样的拦截器。这个类都不会被进行动态代理。

比如下面这个例子：

```java
// 定义了一个全局拦截器，任何通过 Hasor 创建的 Bean 都会被 SimpleInterceptor 进行代理。
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //1.任意类
        Predicate<Class<?>> atClass = Matchers.anyClass();
        //2.任意方法
        Predicate<Method> atMethod = Matchers.anyMethod();
        //3.注册拦截器
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}

// 有一个 Service 标记了 IgnoreProxy 表示不参与动态代理
@IgnoreProxy
public class CoreService {
    ....
}

// 此时在通过 Hasor 创建这个对象时候即便存在全局拦截器也不会对 CoreService 产生任何效果。
AppContext appContext = Hasor.create().build();

appContext.getInstance(CoreService.class)
```

## 标记到包上

如果想控制更大范围内的忽略还可以新建一个 package-info.java 文件将其标记到包上。例如：

```java
@IgnoreProxy
package net.hasor.core;
```

## 传递性

默认情况下 `IgnoreProxy` 的生效范围是其标注的位置以及其所有下属范围都会受到影响。
- 标注到类上被标注的类型及其所有子类都不参与动态代理。
- 通过 `package-info.java` 标注到包上之后：被标注的包下所有类型，及其子包下所有类型都不参与动态代理。

:::tip
在 `@IgnoreProxy` 生效的地方，即便是某些类型上指明了 `@Aop` 这样的注解，动态代理也会被忽略。
:::

这种无差别的向下传播能力有时候并不是想要的，因此可以通过注解的 propagate 属性来控制是否禁止向下传播。这样就可以将忽略范围功效控制在一个可控的范围内。

```java
@IgnoreProxy(propagate = false) // 设置 propagate 属性表示 IgnoreProxy 仅仅在当前类型有效，不影响子类
public class CoreService {
    ....
}
```

## 重写行为

重写行为的前提是我们已经有一个父类或者包上通过 `@IgnoreProxy` 标明了行为，但想在子类或子包中重新定义 `@IgnoreProxy` 的行为。

```java
@IgnoreProxy() // 默认会影响到所有子类
public class FruitService {
    ....
}

// 父类声明了 IgnoreProxy， 禁止行为会传播到 AppleService
public class AppleService extends FruitService {
    ....
}

@IgnoreProxy(ignore = false) // 即便父类禁止了动态代理，子类依然可以通过 ignore 重写控制策略。 
public class SubCoreService extends FruitService {
    ....
}
```
