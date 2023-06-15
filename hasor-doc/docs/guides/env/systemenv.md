---
id: systemenv
sidebar_position: 2
title: a.系统环境变量
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 系统环境变量

## Java原生方式

操作系统环境变量最常见的就是 “JAVA_HOME” 它表示了 Java 的安装位置。我们可以通过一下代码拿到这个环境变量：

```java
System.getenv("JAVA_HOME");
```

例如：“/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home” （我的Mac环境）

## Hasor接口方式

通过 Hasor 框架需要使用 `Environment` 接口获取环境变量。

```java
AppContext appContext = Hasor.create().build();
Environment env = appContext.getEnvironment();
System.out.println(env.getVariable("JAVA_HOME"));
```

结果同上。

## 依赖注入方式

除了上述两种调用接口的方式之外 Hasor 还提供了 @InjectSettings 注解方式来注入配置；针对环境变量的注入，需要使用 ${xxxx} 形式来引入。

```java
// 依赖注入写法
public class TestBean {
    @InjectSettings("${java_home}") // 大小写不敏感
    private String value;

    // get\set ...
}

// 创建Bean并注入环境变量
AppContext appContext = Hasor.create().build();
TestBean testBean = appContext.getInstance(TestBean.class);
System.out.println(testBean.getValue());
```
