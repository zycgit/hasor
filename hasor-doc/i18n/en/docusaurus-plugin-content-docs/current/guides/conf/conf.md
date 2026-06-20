---
id: conf
sidebar_position: 1
title: Configuration Files
description: Supported Hasor configuration file formats and loading methods.
---

# Configuration Files

:::tip
In Hasor, you can use the framework without defining any configuration file. Configuration files are only needed when some configuration must be changed. Hasor embraces convention over configuration, but it does not promote zero configuration.
:::

Hasor supports three configuration file formats, and all of them must use `UTF-8` encoding.
- Properties (recommended file name: `xx-hconfig.properties`)
- YAML (recommended file name: `xx-hconfig.yaml`)
- XML (recommended file name: `xx-hconfig.xml`)

In fact, any file that satisfies XML conventions can be used as a Hasor configuration file. Therefore Hasor's XML configuration file does not provide a corresponding XML Schema.

For a Hasor XML configuration file, the recommended minimum content is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    ...
</config>
```

## Multi-Format Compatibility

Hasor supports three different configuration formats. Although the formats have some differences, they can express the same configuration in common scenarios. For example:

```properties title='Properties format'
mySelf.myName       = Yongchun Zhao
mySelf.myAge        = 12
mySelf.myBirthday   = 1986-01-01 00:00:00
mySelf.myWork       = Software Engineer
mySelf.myProjectURL = https://www.hasor.net/
mySelf.source       = Prop
```

```xml title='XML format'
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="https://www.hasor.net/sechma/main">
    <mySelf>
        <myName>Yongchun Zhao</myName>
        <myAge>12</myAge>
        <myBirthday>1986-01-01 00:00:00</myBirthday>
        <myWork>Software Engineer</myWork>
        <myProjectURL>http://www.hasor.net/</myProjectURL>
        <source>Xml</source>
    </mySelf>
</config>
```

```yaml title='YAML format'
mySelf:
  myName: 'Yongchun Zhao'
  myAge: 12
  myBirthday: '1986-01-01 00:00:00'
  myWork: 'Software Engineer'
  myProjectURL: 'https://www.hasor.net/'
  source: 'Yaml'
```

About the differences:
- Format-specific differences are introduced in later dedicated sections. No matter which configuration format is used, the way configuration is read remains the same.

## Loading Configuration Files

To load a configuration file, specify it with the `mainSettingWith` method during application startup.

```java
AppContext appContext = Hasor.create().mainSettingWith("simple-hconfig.xml").build();
Settings settings = appContext.getInstance(Settings.class);
String myName = settings.getString("mySelf.myName");

// myName is 'Yongchun Zhao'.
```

If the configuration file is named `hconfig.xml` and placed under the classpath, the `mainSettingWith` call can be omitted. Hasor tries to load it by default.

## Placeholders

Configuration values can use `${KEY}` or `${KEY:defaultValue}` placeholders. When loading `Settings`, Hasor resolves placeholders from JVM system properties and operating-system environment variables. If no value is found, the default value is used.

```properties title='Properties format'
jdbc.user = ${JDBC_USER:sa}
jdbc.password = ${JDBC_PASSWORD:password}
```

```xml title='XML format'
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

You can also write configuration values through code during startup:

```java
AppContext appContext = Hasor.create()
        .addSettings(Settings.DefaultNameSpace, "jdbc.user", "sa")
        .addSettings(Settings.DefaultNameSpace, "jdbc.password", "password")
        .build();
```
