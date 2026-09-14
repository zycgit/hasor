---
id: conf
sidebar_position: 1
title: 2.8 配置文件
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.8 配置文件

:::tip
在 Hasor 中 可以不定义任何配置文件，直接使用 Hasor 相关功能。仅仅在当需要修改某些配置的时候在使用配置文件，Hasor 对于配置文件的态度是拥抱约定优于配置，但是不鼓吹零配置。
:::

Hasor 支持三种格式的配置文件，目前必须要求使用 `UTF-8` 编码格式。
- Properties （建议文件名 xx-hconfig.properties）
- YAML（建议文件名 xx-hconfig.yaml）
- XML（建议文件名 xx-hconfig.xml）

事实上任何一个满足 Xml 约定的文件都可以被用来充当 Hasor 的配置文件，因此 Hasor 的Xml 配置文件没有提供对应的 Xml Schema。

但作为 Hasor 的 xml 配置文件我们建议您最少内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    ...
</config>
```

## 多格式兼容

Hasor 同时支持三种不同的配置文件格式，虽然不同格式的配置文件具有一些差异性，但在通用场景下可以表述相同的一段配置内容。例如：

```properties title='属性文件格式'
mySelf.myName       = 赵永春
mySelf.myAge        = 12
mySelf.myBirthday   = 1986-01-01 00:00:00
mySelf.myWork       = Software Engineer
mySelf.myProjectURL = https://www.hasor.net/
mySelf.source       = Prop
```

```xml title='XML 格式'
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="https://www.hasor.net/sechma/main">
    <mySelf>
        <myName>赵永春</myName>
        <myAge>12</myAge>
        <myBirthday>1986-01-01 00:00:00</myBirthday>
        <myWork>Software Engineer</myWork>
        <myProjectURL>http://www.hasor.net/</myProjectURL>
        <source>Xml</source>
    </mySelf>
</config>
```

```yaml title='YAML 格式'
mySelf:
  myName: '赵永春'
  myAge: 12
  myBirthday: '1986-01-01 00:00:00'
  myWork: 'Software Engineer'
  myProjectURL: 'https://www.hasor.net/'
  source: 'Yaml'
```

关于差异性
- 具体差异性，会在后面专属章节来介绍。无论配置文件格式选择是哪一个，对于配置的读取方式始终相同。

## 加载配置文件

加载配置文件只需要在应用启动的时候通过 `mainSettingWith` 方法指定一下配置文件即可。

```java
AppContext appContext = Hasor.create().mainSettingWith("simple-hconfig.xml").build();
Settings settings = appContext.getInstance(Settings.class);
String myName = settings.getString("mySelf.myName");

// myName 值为 ‘赵永春’
```

将配置文件的名字改为 “hconfig.xml” 并且放到 classpath 下面，这样就可以省去调用 `mainSettingWith` 方法。Hasor 会默认尝试加载它。

## 占位符

配置值可以使用 `${KEY}` 或 `${KEY:defaultValue}` 占位符。Hasor 在加载 Settings 时会从 JVM 系统属性和操作系统环境变量中解析占位符，未找到值时使用默认值。

```properties title='属性文件格式'
jdbc.user = ${JDBC_USER:sa}
jdbc.password = ${JDBC_PASSWORD:password}
```

```xml title='XML 格式'
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <jdbc>
        <user>${JDBC_USER:sa}</user>
        <password>${JDBC_PASSWORD:password}</password>
    </jdbc>
</config>
```

```java
AppContext appContext = Hasor.create().mainSettingWith("hconfig.xml").build();
Settings settings = appContext.getSettings();
String user = settings.getString("jdbc.user");
```

也可以在启动时通过代码写入配置：

```java
AppContext appContext = Hasor.create()
        .addSettings(Settings.DefaultNameSpace, "jdbc.user", "sa")
        .addSettings(Settings.DefaultNameSpace, "jdbc.password", "password")
        .build();
```
