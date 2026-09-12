---
id: ioc
sidebar_position: 1
title: Dependency Injection (IoC)
description: Introduces dependency injection and inversion of control in Hasor.
---

# Dependency Injection (IoC)

"Dependency injection" (DI) is sometimes also called "inversion of control" (IoC). In essence, they describe the same idea. Normally, when one class calls another class, the caller creates the callee. With inversion of control, the caller no longer creates the callee directly; the container injects it instead. The word "create" here emphasizes the caller's active role. Dependency injection removes the need for the caller to actively create the object it depends on.

For example, in the usual case the caller (`ClassA`) first creates the callee (`FunBean`) and then calls the `foo` method of `FunBean` from `callFoo`:

```java title='Example'
public class ClassA {
    private FunBean funBean = new FunBean();

    public void callFoo() {
        this.funBean.foo();
    }
}

public class FunBean {
    public void foo() {
        System.out.println("say ...");
    }
}
```

With dependency injection, the situation is the opposite. The caller (`ClassA`) does not know ahead of time which callee (`FunBean`) to create. `ClassA` calls the injected `FunBean`. Usually, objects that need dependency injection expose a setter method. Before `callFoo` is called, the `funBean` object is set through `setFunBean`.

```java title='Example'
public class ClassA {
    private FunBean funBean = null;

    public void setFunBean(FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();
    }
}

public class FunBean {
    ...
}
```

Strictly speaking, injection has two forms: constructor injection and property injection.

We often hear about a third form called interface injection. It is actually an interface-oriented expression of property injection.

## Constructor Injection

This means that the injected object is passed through the constructor:

```java title='Example'
public class ClassA {
    private FunBean funBean = null;

    public ClassA(FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();
    }
}
```

## Property Injection

This means that the injected object is passed through getter/setter-style property methods:

```java title='Example'
public class ClassA {
    private FunBean funBean = null;

    public void setFunBean(FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();
    }
}
```

## Interface Injection

Interface injection injects through a setter method defined by an interface. As you can see, it is still **property injection** in essence; the caller (`ClassA`) simply implements an injection interface.

```java title='Example'
public interface IClassA {
    public void setFunBean(FunBean funBean);
}

public class ClassA implements IClassA {
    private FunBean funBean = null;

    public void setFunBean(FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();

    }
}
```

We have now explained what dependency injection is and shown several forms of it. In the next section, we will see how Hasor helps you perform dependency injection.

## Circular Dependency Diagnostics

When a Bean creation chain closes a cycle, the container throws `net.hasor.core.CircularDependencyException`. The message lists dependencies and marks the closing node; `getDependencyPath()` returns an immutable path including the repeated final node. `hasor-config` `@Bean` factories include their configuration class, method, and parameter types in the description. Break the dependency cycle or defer resolution through a Provider; resolving that Provider immediately within the same creation chain still forms a cycle.
