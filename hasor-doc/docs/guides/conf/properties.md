---
id: properties
sidebar_position: 2
title: a.Properties 格式差异性
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 配置文件

属性文件格式上没有什么特殊要说明的，就是常见的 Key/Value 格式。

:::caution
唯一要说明的是，当加载多个属性文件之后。如遇到相同的 key 配置会被覆盖。如果要避免这一点可以选择 XML 格式并通过命名空间加以隔离。
:::

配置文件被加载之后按照 key 的值读取即可。例如：

```properties title='属性文件格式'
mySelf.myName       = 赵永春
mySelf.myAge        = 12
mySelf.myBirthday   = 1986-01-01 00:00:00
mySelf.myWork       = Software Engineer
mySelf.myProjectURL = https://www.hasor.net/
mySelf.source       = Prop
```

```java
Settings settings = ...
String myName = settings.getString("mySelf.myName");
 
// myName 值为 ‘赵永春’
```
