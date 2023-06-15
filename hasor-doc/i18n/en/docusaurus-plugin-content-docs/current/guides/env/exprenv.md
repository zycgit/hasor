---
id: exprenv
sidebar_position: 5
title: d.环境变量表达式
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 环境变量表达式

## 引用环境变量

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.environmentVar>
        <MY_VAR>Hello Word , this is my JAVA_HOME : %JAVA_HOME%</MY_VAR>
    </hasor.environmentVar>
</config>
```

`MY_VAR` 的输出结果是：`Hello Word , this is my JAVA_HOME : xxxxxx`

在比如同时引入多个环境变量：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.environmentVar>
        <MY_NAME>zyc</MY_NAME>
        <MY_AGE>100</MY_>
        <MY_VAR>my name is : %MY_NAME% , age is %MY_AGE%</MY_VAR>
    </hasor.environmentVar>
</config>
```

`MY_VAR` 的输出结果是：`my name is : zyc , age is 100`

## 通过接口方式来引用

代码层面可以通过 `Environment` 接口的 `evalString` 方法来计算这个表达式

```java
AppContext appContext = Hasor.create()//
    .addVariable("MY_NAME", "zyc")     //
    .addVariable("MY_AGE", "34")       //
    .build();

String envTemp = "my name is %MY_NAME% ,age is %MY_AGE%";
String evalString = appContext.getEnvironment().evalString(envTemp);
System.out.println(evalString);
```

上述代码执行之后 evalString 的值会是：`“my name is zyc ,age is 34”`
