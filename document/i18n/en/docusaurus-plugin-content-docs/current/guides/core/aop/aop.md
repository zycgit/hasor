---
id: aop
sidebar_position: 1
title: Dynamic Proxy (AOP)
description: Introduces aspect-oriented programming and dynamic proxy support in Hasor.
---

# What Is AOP?

"Aspect-oriented programming", also known as "AOP", is a very active development idea. AOP can isolate different parts of business logic, reduce coupling between those parts, improve code reuse, and improve development efficiency.

The purpose of AOP programming is to separate cross-cutting code, such as logging, performance statistics, security control, transactions, and exception handling, from business logic.

For example, suppose there is an interface for querying user information and you now need to add logging to it. Every query should record its execution time. Normally, implementing this feature means inserting code before and after every method in the interface implementation class to collect data. That is cumbersome, while AOP makes the solution elegant.

There are two ways to implement the AOP programming model: **static proxy** and **dynamic proxy**.
- **Static proxy** usually appears as the Proxy Pattern.
- **Dynamic proxy** has many forms, such as Java's native `Proxy`, CGLib, JBossAOP, and others.

## Static Proxy

Assume there is a factory where workers need to clock in when they start and clock out when they leave. A worker can be represented by the `Worker` interface, and the work action can be represented by the `doWork` method.

```java title='An object-oriented worker'
public interface Worker {
    public void doWork();
}
```

Clocking in and clocking out are separated into two actions. We can model a time clock and represent those actions with `beforeWork` and `afterWork`:

```java
public class Machine {
    public void beforeWork() {
        ...
    }

    public void afterWork() {
        ...
    }
}
```

The factory considers an employee clocked in as soon as the employee enters the factory, and clocked out when leaving. To make attendance friendlier, the company uses a modern technique that lets employees avoid manual clocking, as if each person had a personal assistant. This technique simply surrounds the worker's `doWork` execution with automatic clock-in and clock-out logic. Its abstraction is shown below:

```java
public class WorkerProxy implements Worker {
    private Machine machine;
    private Worker targetWorker;

    public void doWork() {
        this.machine.beforeWork();
        this.targetWorker.doWork();
        this.machine.afterWork();
    }
}
```

## Dynamic Proxy

With static proxy, the proxy class (`WorkerProxy`) already exists as a class file when the program runs. Dynamic proxy is the opposite: the proxy class does not exist ahead of time. It is created by specialized libraries when needed.

For example, suppose a program has many different service classes, and we need to print how long each business method takes. With static proxy, there is no fixed `doWorker` method in the program.

Even though there is no fixed `doWorker` method, the invocation behavior exists. That behavior can be abstracted as an "aspect" in AOP, and the class responsible for executing the aspect is called an "interceptor". The following code shows how to implement dynamic proxy with Java's native support.

```java title='Example'
ClassLoader lod = Thread.currentThread().getContextClassLoader();

Class<?>[] faceSet = new Class[] { TestBean2_Face.class };

Object proxy = Proxy.newProxyInstance(
        lod, faceSet, new JavaInvocationHandler()
);

TestBean2_Face face = (TestBean2_Face) proxy;

System.out.println(face.toString());
```

The interceptor used in the example above is the `JavaInvocationHandler` class.

```java
class JavaInvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) {
        return null; // TODO Auto-generated method stub
    }
}
```

This shows that implementing a dynamic proxy in Java is not very difficult. Sometimes, however, we want to manage all beans and apply dynamic proxies to them according to our own rules. That requirement means building a bean container, and Hasor provides exactly that capability.
