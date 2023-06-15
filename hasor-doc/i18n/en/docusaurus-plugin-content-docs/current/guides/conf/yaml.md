---
id: yaml
sidebar_position: 3
title: b.YAML 格式差异性
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 配置文件

YAML 格式也是较为流行的一种配置文件格式，它的最大优点是 `可以表述数组类型`。并且在阅读层面比属性文件更加有效直观。\

:::caution
唯一要说明的是，当加载多个属性文件之后。如遇到相同的 key 配置会被覆盖。如果要避免这一点可以选择 XML 格式并通过命名空间加以隔离。
:::

```yaml title='YAML 格式'
mySelf:
  myName: '赵永春'
  myAge: 12
  myBirthday: '1986-01-01 00:00:00'
  myWork: 'Software Engineer'
  myProjectURL: 'http://www.hasor.net/'
  source: 'Yaml'
arrays: [ 'a','b' ]
```

```java
Settings settings = ...
String myName = settings.getString("mySelf.myName");
// myName 值为 ‘赵永春’

assert settings.getString("arrays").equals("b");
assert settings.getStringArray("arrays")[0].equals("a");
assert settings.getStringArray("arrays")[1].equals("b");
```
