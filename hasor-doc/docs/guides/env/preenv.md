---
id: preenv
sidebar_position: 4
title: c.预定义环境变量
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 预定义环境变量

:::tip
预定义环境变量是指：这个环境变量无论是在系统层面还是在Java系统属性层面都没有被声明。

但是应用在运行的时候无论是否已经定义这个环境变量，应用都可以通过获取环境变量都方式获取到一个配置值。而最初始的值就是预定义环境变量。
:::

## 配置文件方式

在配置文件中声明环境变量，必须要在 hasor.environmentVar 节点中配置。在配置文件中通过 “hasor.environmentVar” 节点来声明 MY_VAR 环境变量的默认值。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.environmentVar>
        <MY_VAR>Hello Word , this is my JAVA_HOME : %JAVA_HOME%</MY_VAR>
    </hasor.environmentVar>
</config>
```

在项目中获取这个环境变量：

```java
AppContext appContext = Hasor.create().mainSettingWith(...).build();
Environment env = appContext.getEnvironment();
System.out.println(env.getVariable("MY_VAR"));
```

程序执行之后会得到 `“Hello Word , this is my JAVA_HOME : /Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home”` （JAVA_HOME 依据环境而不同）

## 程序编码方式

```java
AppContext appContext = Hasor.create()//
    .addVariable("MY_VAR", "zyc")      //
    .build();

Environment env = appContext.getEnvironment();
System.out.println(env.getVariable("MY_VAR"));
```

程序执行之后会得到 “zyc”
