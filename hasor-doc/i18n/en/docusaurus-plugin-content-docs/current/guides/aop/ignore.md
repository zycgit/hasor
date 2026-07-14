---
id: ignore
sidebar_position: 7
title: f. Ignoring Dynamic Proxy
description: Use @IgnoreProxy to exclude classes or packages from dynamic proxying.
---

# Ignoring Dynamic Proxy

## Marking a Class

Any class or package can be annotated with `@IgnoreProxy` to prevent dynamic proxying.

```java
@IgnoreProxy
public class CoreService {
    ....
}
```

At this point, when this bean is created through `AppContext`, the class will not be dynamically proxied no matter what interceptors are configured.

For example:

```java
// A global interceptor is defined. Any bean created by Hasor will be proxied by SimpleInterceptor.
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // 1. Any class.
        Predicate<Class<?>> atClass = Matchers.anyClass();
        // 2. Any method.
        Predicate<Method> atMethod = Matchers.anyMethod();
        // 3. Register the interceptor.
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}

// A service is marked with IgnoreProxy, meaning it does not participate in dynamic proxying.
@IgnoreProxy
public class CoreService {
    ....
}

// When Hasor creates this object, the global interceptor has no effect on CoreService.
AppContext appContext = Hasor.create().build();

appContext.getInstance(CoreService.class)
```

## Marking a Package

To ignore a wider range, create a `package-info.java` file and place the annotation on the package. For example:

```java
@IgnoreProxy
package net.hasor.core;
```

## Propagation

By default, the effective range of `IgnoreProxy` is the annotated location and all subordinate ranges.
- When placed on a class, the annotated type and all its subclasses do not participate in dynamic proxying.
- When placed on a package through `package-info.java`, all types under the annotated package and all types under its subpackages do not participate in dynamic proxying.

:::tip
Where `@IgnoreProxy` is effective, dynamic proxying is ignored even if some types explicitly specify annotations such as `@Aop`.
:::

This indiscriminate downward propagation is not always desired. Use the `propagate` attribute of the annotation to control whether downward propagation is disabled, keeping the ignore range under control.

```java
@IgnoreProxy(propagate = false) // Set propagate to make IgnoreProxy effective only on the current type, without affecting subclasses.
public class CoreService {
    ....
}
```

## Overriding Behavior

Overriding behavior applies when a parent class or package already declares behavior through `@IgnoreProxy`, but a subclass or subpackage needs to redefine that behavior.

```java
@IgnoreProxy() // By default, this affects all subclasses.
public class FruitService {
    ....
}

// The parent class declares IgnoreProxy, so the disabled behavior propagates to AppleService.
public class AppleService extends FruitService {
    ....
}

@IgnoreProxy(ignore = false) // Even if the parent disables dynamic proxying, the subclass can override the policy through ignore.
public class SubCoreService extends FruitService {
    ....
}
```
