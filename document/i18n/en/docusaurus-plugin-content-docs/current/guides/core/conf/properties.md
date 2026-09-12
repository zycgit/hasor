---
id: properties
sidebar_position: 2
title: Properties Format Differences
description: Notes about using properties files with Hasor Settings.
---

# Properties Format Differences

There is nothing special about the properties-file format. It is the common key/value format.

:::caution
The only point to note is that when multiple properties files are loaded, duplicate keys overwrite earlier values. To avoid this, use XML and isolate configuration with namespaces.
:::

After a configuration file is loaded, read values by key. For example:

```properties title='Properties format'
mySelf.myName       = Yongchun Zhao
mySelf.myAge        = 12
mySelf.myBirthday   = 1986-01-01 00:00:00
mySelf.myWork       = Software Engineer
mySelf.myProjectURL = https://www.hasor.net/
mySelf.source       = Prop
```

```java
Settings settings = ...
String myName = settings.getString("mySelf.myName");
 
// myName is 'Yongchun Zhao'.
```
