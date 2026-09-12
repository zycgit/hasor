---
id: modulelife
sidebar_position: 5
title: Module Lifecycle
description: Use Module callbacks and SPI listeners during the Hasor lifecycle.
---

# Module Lifecycle

## Using the Module Interface

The following small example demonstrates Hasor lifecycle behavior. First, create a class that implements the `Module` interface. It prints one log line when each lifecycle phase arrives.

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        logger.info("initializing...");
    }

    public void onStart(AppContext appContext) throws Throwable {
        logger.info("started...");
    }

    public void onStop(AppContext appContext) throws Throwable {
        logger.info("stopped...");
    }
}
```

Next, start Hasor in the simplest way and load this module. After Hasor starts, the console prints "initializing..." and then "started...". When the JVM exits, the console prints "stopped...".

## Using the SPI Mechanism

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {

        apiBinder.bindSpiListener(ContextInitializeListener.class, new ContextInitializeListener() {
            public void doInitializeCompleted(AppContext templateAppContext) {
                logger.info("initialization completed...");
            }
        });

        apiBinder.bindSpiListener(ContextStartListener.class, new ContextStartListener() {
            public void doStart(AppContext appContext) {
                logger.info("starting...");
            }
            public void doStartCompleted(AppContext appContext) {
                logger.info("start completed...");
            }
        });

        apiBinder.bindSpiListener(ContextShutdownListener.class, new ContextShutdownListener() {
            public void doShutdown(AppContext appContext) {
                logger.info("stopping...");
            }
            public void doShutdownCompleted(AppContext appContext) {
                logger.info("stop completed...");
            }
        });
    }
}
```

Using SPI gives you more lifecycle phases than `Module`: "initialization completed...", "starting...", "start completed...", "stopping...", and "stop completed...".

The event mechanism has only two events: "container started..." and "container stopped...".
