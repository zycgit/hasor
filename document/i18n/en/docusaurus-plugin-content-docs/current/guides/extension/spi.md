---
id: spi
sidebar_position: 2
title: SPI
description: Use Hasor SPI extension points to insert custom behavior into framework flows.
---

# SPI

:::tip
SPI stands for Service Plugin Interface. Its original intent is to insert extension points into an application's execution flow.
These extension points let an otherwise fixed code flow become dynamically extensible and can even affect execution state.

The current version supports Java's standard SPI declaration style, namely `META-INF/services/xxxx`.
:::

SPI has two modes. Their working principles are shown below:

![](../_img/spi-theory.png)

## notifySpi (Notification Type)

When each SPI is called, listeners cannot affect each other. If a `notifySpi` execution has a return value, that return value is used as the return value after the SPI call is initiated.

However, when one `notifySpi` has multiple listeners, the SPI call can still have only one return value. Therefore, `SpiJudge` is needed to select the return value. The default `SpiJudge` selects the last value.

## chainSpi (Chain Type)

A chain-type SPI is similar to an AOP interceptor or a filter. Its purpose is to allow SPI listeners to have before/after dependencies. With this relationship, SPI listeners can implement more complex logic.

:::tip
SPI naming:
- Normally, `notifySpi` listeners are named in the form `xxxListener`.
- `chainSpi` listeners are often named in the form `xxxxChainSpi`.
:::

## SPI Listeners

Whether it is `notifySpi` or `chainSpi`, an SPI listener must extend or implement the `java.util.EventListener` interface. Beyond that, there is no essential difference between the two SPI types when they are defined.

## SPI Triggers

The main difference between `ChainSpi` and `NotifySpi` is that different SPI trigger methods are used, which then execute different SPI processing flows.

Assume a simple example that prints three lines to the console. The program looks like this:

```java
System.out.println("A");
System.out.println("B");
System.out.println("C");
```

Now we want to insert several steps before printing B without affecting the code flow. These steps are dynamically registered through SPI. First, declare an SPI interface.

```java
public interface MySpiListener extends EventListener {
    public void doSpi();
}
```

Then modify the existing code and insert the SPI invocation logic at the appropriate location.

```java
@Inject
private SpiTrigger spiTrigger;

System.out.println("A");
// do spi
spiTrigger.notifySpiWithoutResult(MySpiListener.class, new SpiCallerWithoutResult<MySpiListener>() {
    public void doSpi(MySpiListener listener) throws Throwable {
        listener.doSpi();
    }
});

System.out.println("B");
System.out.println("C");
```

Finally, register `MySpiListener` during module loading.

```java
public class RootModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.bindSpiListener(MySpiListener.class, new MySpiListenerImpl());
        ...
    }
}
```

## SPI Judge

A judge has two responsibilities:
- It can decide which SPI listeners finally execute and in what order.
- It helps notification-type SPI calls decide which return value to use.

```java
// Register listeners.
AppContext appContext = Hasor.create().build(apiBinder -> {
    apiBinder.bindSpiListener(TestSpi.class, (obj) -> {
        ...
        return dataA;
    });

    apiBinder.bindSpiListener(TestSpi.class, (obj) -> {
        ...
        return dataB;
    });

    apiBinder.bindSpiJudge(TestSpi.class, new SpiJudge() {
        // Change the default judge behavior and select the first value.
        public <R> R judgeResult(List<R> result, R defaultResult) {
            return result.get(0);
        }

        // Decide which SPI listeners are effective and their order.
        public <T extends java.util.EventListener> List<T> judgeSpi(List<T> spiListener) {
            return spiListener;
        }
    });
});

// Trigger the SPI call.
SpiTrigger spiTrigger = appContext.getInstance(SpiTrigger.class);
Object resultSpi = spiTrigger.notifySpi(TestSpi.class, new SpiCaller<TestSpi, Object>() {
    public Object doResultSpi(TestSpi listener, Object lastResult) throws Throwable {
        return listener.doSpi(lastResult);
    }
}, defaultResult);

// With two SPI listeners, the default judge returns the last value, dataB, rather than dataA.
assert resultSpi == dataA;
```
