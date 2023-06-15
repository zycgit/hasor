---
id: javaenv
sidebar_position: 3
title: b.Java 系统属性
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# Java 系统属性

## 获取Java系统属性

Java 的系统属性是指，除了操作系统环境变量之外。JVM 本身的系统属性，这些属性通常包含了 JVM 的一些运行信息。例如：“java.version”

```java
System.getProperty("java.version")
```

例如：“1.8.0_151” （我本地的 Java版本）

作为 Java 系统属性还可以通过 java 命令的 -D 参数进行设置，例如：java -DMY_NAME=ccc -jar ./example.jar

```java
System.getProperty("MY_NAME")
```

## Hasor接口方式获取

通过 Hasor 框架使用 Environment 接口获取系统属性。

```java
AppContext appContext = Hasor.create().build();
Environment env = appContext.getEnvironment();
System.out.println(env.getVariable("java.version"));
```

例如：“1.8.0_151” （我的Mac环境）

## 依赖注入方式

除了上述两种调用接口的方式之外 Hasor 还提供了 @InjectSettings 注解方式来注入配置；针对环境变量的注入，需要使用 ${xxxx} 形式来引入。

```java
// 依赖注入写法
public class TestBean {
    @InjectSettings("${java.version}") // 大小写不敏感
    private String value;

    // get\set ...
}

// 创建Bean并注入环境变量
AppContext appContext = Hasor.create().build();
TestBean testBean = appContext.getInstance(TestBean.class);
System.out.println(testBean.getValue());
```
