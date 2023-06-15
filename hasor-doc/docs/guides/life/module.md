---
id: modulelife
sidebar_position: 5
title: d.Module 生命周期
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# Module 生命周期

## 使用 Module 接口

现在我们用一个小例子来想你展示 Hasor 生命周期的特征，首先我们新建一个类，这个类实现了 Module 接口。我们在每一个周期到来时打印一行日志。

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        logger.info("初始化拉...");
    }

    public void onStart(AppContext appContext) throws Throwable {
        logger.info("启动啦...");
    }

    public void onStop(AppContext appContext) throws Throwable {
        logger.info("停止啦...");
    }
}
```

接下来我们用最简单的方式启动 Hasor 并加载这个 Module，当 Hasor 启动之后我们可以看到控制台上先后打印出 “初始化拉...”、“启动啦...”，当 JVM 退出时我们还会看到控制台打印 “停止啦...”。

## 使用SPI机制

```java
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {

        apiBinder.bindSpiListener(ContextInitializeListener.class, new ContextInitializeListener() {
            public void doInitializeCompleted(AppContext templateAppContext) {
                logger.info("初始化完成啦...");
            }
        });

        apiBinder.bindSpiListener(ContextStartListener.class, new ContextStartListener() {
            public void doStart(AppContext appContext) {
                logger.info("开始启动啦...");
            }
            public void doStartCompleted(AppContext appContext) {
                logger.info("启动完成啦...");
            }
        });

        apiBinder.bindSpiListener(ContextShutdownListener.class, new ContextShutdownListener() {
            public void doShutdown(AppContext appContext) {
                logger.info("开始停止啦...");
            }
            public void doShutdownCompleted(AppContext appContext) {
                logger.info("停止完成啦...");
            }
        });
    }
}
```

使用 SPI 会得到比 Module 更多的生命周期阶段。“初始化完成啦...”、“开始启动啦...”、“启动完成啦...”、“开始停止啦...”、“停止完成啦...”

事件机制只有两种：“容器启动啦...”、“容器停止啦...”
