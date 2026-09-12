---
id: yaml
sidebar_position: 3
title: YAML Format Differences
description: Notes about using YAML configuration with Hasor Settings.
---

# YAML Format Differences

YAML is also a popular configuration-file format. Its biggest advantage is that it can express array types, and it is more readable and intuitive than properties files.

:::caution
The only point to note is that when multiple properties files are loaded, duplicate keys overwrite earlier values. To avoid this, use XML and isolate configuration with namespaces.
:::

```yaml title='YAML format'
mySelf:
  myName: 'Yongchun Zhao'
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
// myName is 'Yongchun Zhao'.

assert settings.getString("arrays").equals("b");
assert settings.getStringArray("arrays")[0].equals("a");
assert settings.getStringArray("arrays")[1].equals("b");
```
