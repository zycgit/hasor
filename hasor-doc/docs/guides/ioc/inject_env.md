---
id: envioc
sidebar_position: 8
title: g.注入环境变量
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 注入环境变量

把敏感信息通过环境参数传递给应用是一个十分安全的做法，Hasor 支持注入一个环境参数。例如：

```java
public class DataBaseBean {
    @InjectSettings("${db.user}")
    private String user;
    
    @InjectSettings("${db.pwd}")
    private String password;
    
    ...
}
```

然后当启动程序时，追加两个 `-D` 参数即可：`java TestMain -Ddb.user=username -Ddb.pwd=password`

除了 `-D` 参数之外，环境变量还可以是系统环境变量。例如得到 `JAVA_HOME 位置。

```java
public class DataBaseBean {
    @InjectSettings("${JAVA_HOME}")
    private String javaHome;
}
```

:::tip
这些位置可以成为 Hasor 环境变量的来源。
:::

位置
- `System.getenv()`
- `System.getProperties()`
- `hconfig.xml` 配置文件中 `hasor.environmentVar` 的子节点
- `Hasor.create().addVariable(...)`